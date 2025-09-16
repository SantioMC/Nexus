package me.santio.nexus

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.protocol.chat.ChatType
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import io.leangen.geantyref.TypeToken
import me.santio.nexus.api.Nexus
import me.santio.nexus.api.NexusConfig
import me.santio.nexus.api.nexus
import me.santio.nexus.command.NexusCommand
import me.santio.nexus.command.Parser
import me.santio.nexus.command.SuggestionProvider
import me.santio.nexus.config.NexusPaperConfig
import me.santio.nexus.data.UUID
import me.santio.nexus.data.node.NodeImpl
import me.santio.nexus.data.node.NodeManagerImpl
import me.santio.nexus.data.player.PlayerManagerImpl
import me.santio.nexus.data.serializer.gson.ChatTypeAdapter
import me.santio.nexus.data.serializer.gson.ComponentAdapter
import me.santio.nexus.data.serializer.gson.InstantAdapter
import me.santio.nexus.ext.async
import me.santio.nexus.ext.packetevents
import me.santio.nexus.ext.silentSave
import me.santio.nexus.io.S3
import me.santio.nexus.io.provided.NexusNats
import me.santio.nexus.packet.node.NodeConnectedPacket
import me.santio.nexus.packet.node.NodeDisconnectedPacket
import me.santio.nexus.packet.node.NodeTopographyRequestPacket
import me.santio.nexus.service.NexusListener
import me.santio.nexus.service.Preload
import me.santio.nexus.tasks.ChunkTrackerTask
import me.santio.nexus.tasks.NodeHeartbeatTask
import me.santio.nexus.tasks.VirtualEntityTicker
import me.santio.nexus.virtual.PacketEventsVirtualizer
import me.santio.nexus.world.S3WorldSynchronizer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.LinkedTransferQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.properties.Delegates

class NexusPlugin: JavaPlugin(), Nexus {

    private lateinit var nexusConfig: NexusConfig
    private lateinit var identifier: String

    private val worldCache = mutableMapOf<String, S3WorldSynchronizer>()
    internal lateinit var s3: S3

    private val handleThreadFactory = Thread.ofPlatform()
        .name("nexus-", 1)
        .group(ThreadGroup("Nexus"))
        .daemon(true)
        .factory()

    internal val scheduler = Executors.newScheduledThreadPool(16, handleThreadFactory)
    internal val executor = ThreadPoolExecutor(4, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
        LinkedTransferQueue(), handleThreadFactory)

    internal val miniMessage = MiniMessage.builder()
        .editTags {
            it.resolver(Placeholder.unparsed("icon-emoji", ICON))
            it.resolver(Placeholder.parsed("icon", "<primary><icon-emoji></primary>"))
            it.resolver(Placeholder.styling("primary", primaryColor))
            it.resolver(Placeholder.styling("secondary", secondaryColor))
            it.resolver(Placeholder.styling("body", NamedTextColor.GRAY))
            it.resolver(Placeholder.styling("success", TextColor.color(0x66aa77)))
            it.resolver(Placeholder.styling("error", TextColor.color(0xa86565)))
        }
        .build()

    var onlineAt by Delegates.notNull<Long>()
    val gson: Gson = GsonBuilder() // Gson used for classes that may change a lot & are internal (nms / packetevents)
        .registerTypeAdapter(Instant::class.java, InstantAdapter)
        .registerTypeAdapter(ChatType::class.java, ChatTypeAdapter)
        .registerTypeAdapter(Component::class.java, ComponentAdapter)
        .create()

    override fun onLoad() {
        saveDefaultConfig()

        nexusConfig = NexusPaperConfig(config)
        s3 = S3(config)

        // Generate an identifier
        val path = dataPath.resolve(".id")
        identifier = path.takeIf { it.exists() }?.readText()
            ?: generateId().also { path.createFile().writeText(it) }

        // Configure PacketEvents
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        packetevents.load()

        // Register the Nexus API
        server.servicesManager.register(
            Nexus::class.java,
            this,
            this,
            ServicePriority.High
        )

        // Connect to the cluster
        cluster().connect(config)

        // Connect to the remote storage
        s3.connect()

        // Pull in worlds from s3
        server.worldContainer.listFiles()
            .filter { it.isDirectory }
            .filter { it.resolve("level.dat").exists() }
            .map { nexus.world(it.name) }
            .forEach { it.download() }
    }

    @Suppress("UnstableApiUsage")
    override fun onEnable() {
        packetevents.init()
        markReady()

        // Register Bukkit listeners
        ServiceLoader.load(Listener::class.java, this::class.java.classLoader)
            .forEach { server.pluginManager.registerEvents(it, this) }

        // Register PacketEvents listeners
        ServiceLoader.load(PacketListener::class.java, this.javaClass.getClassLoader())
            .forEach { packetevents.eventManager.registerListener(it, PacketListenerPriority.LOWEST) }

        // Register commands
        val commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
                .buildOnEnable(this)

        val annotationParser = AnnotationParser(commandManager, Source::class.java)

        ServiceLoader.load(Parser::class.java, this.javaClass.classLoader).forEach {
            commandManager.parserRegistry().registerParserSupplier(TypeToken.get(it.classType())) { _ -> it }
        }

        ServiceLoader.load(SuggestionProvider::class.java, this.javaClass.classLoader).forEach { annotationParser.parse(it) }
        ServiceLoader.load(NexusCommand::class.java, this.javaClass.classLoader).forEach { annotationParser.parse(it) }

        // Preload classes required for shutdown
        ServiceLoader.load(Preload::class.java, this.javaClass.classLoader).forEach { it.preload() }
    }

    override fun onDisable() {
        // When working with symlinks, an error is produced since the class can't be resolved
        val packet = try { NodeDisconnectedPacket() } catch (_: NoClassDefFoundError) { null }
        packet?.let { nexus.cluster().publish(it) }

        // Save the worlds ourselves so they're written to disk, so that we can zip and store them
        println("[Nexus] Saving ${server.worlds.size} worlds to remote storage...")
        server.worlds.forEach {
            it.silentSave(true)
            nexus.world(it).save()
        }

        println("[Nexus] Node is now offline, goodbye!")
        packetevents.terminate()
    }

    private fun generateId() = UUID.randomUUID().toString().substringAfterLast("-")

    private fun markReady() {
        onlineAt = Instant.now().toEpochMilli()

        // Announce ourselves to the cluster as ready
        nexus.cluster().publish(NodeConnectedPacket(onlineAt))
        val self = NodeImpl(nexus.id(), onlineAt)

        // Reload & restart support
        self.players.addAll(server.onlinePlayers.map { it.uniqueId })
        self.entities.addAll(server.worlds.flatMap { it.entities }.map { it.uniqueId })

        // Register node
        NodeManagerImpl.add(self)

        // Register listeners
        ServiceLoader.load(NexusListener::class.java, this::class.java.classLoader)
            .forEach { it.register() }

        // Ask for the current list of nodes
        nexus.cluster().publish(NodeTopographyRequestPacket())

        // Start sending out heartbeats
        scheduler.scheduleAtFixedRate(NodeHeartbeatTask, 2, 2, TimeUnit.SECONDS)

        // Start ticking virtual entities
        scheduler.scheduleAtFixedRate(VirtualEntityTicker, 250, 250, TimeUnit.MILLISECONDS)

        // Start tracking async events
        scheduler.scheduleAtFixedRate(ChunkTrackerTask, 50, 50, TimeUnit.MILLISECONDS)

        // Wait a few seconds before finding the master node
        async(delay = 60) {
            // If no nodes
            if (nexus.nodes().size() <= 1) {
                plugin.logger.info("No online nodes exist, electing self")
                nexus.nodes().electSelf()
                return@async
            }

            // If no master, or master is slow at sending topology
            nexus.nodes().master()?.let {
                plugin.logger.info("No master node was found, electing best known node")
                nexus.nodes().electBest()
            }
        }

        // Record how many servers we discovered
        async(delay = 20) {
            plugin.logger.info("Discovered ${nexus.nodes().size() - 1} nodes in the cluster")
        }
    }

    override val entityNodeKey: NamespacedKey = NamespacedKey(this, "node")

    override fun id() = identifier
    override fun cluster() = NexusNats
    override fun virtualization() = PacketEventsVirtualizer
    override fun players() = PlayerManagerImpl
    override fun nodes() = NodeManagerImpl
    override fun config() = nexusConfig
    override fun world(world: World) = world(world.name)
    override fun world(world: String) = worldCache.getOrPut(world) { S3WorldSynchronizer(world) }

    override fun self() = nodes().self()

    override fun player(uniqueId: java.util.UUID) = players().get(uniqueId)
    override fun player(player: Player) = player(player.uniqueId)
        ?: error("The player ${player.name} is not online, they have no data")

    internal companion object {
        val plugin by lazy { getPlugin(NexusPlugin::class.java) }

        const val ICON = "\uD83C\uDF00"
        val primaryColor = TextColor.color(0xdfa3e2)
        val secondaryColor = TextColor.color(0x7e9ede)
    }

}