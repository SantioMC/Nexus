package me.santio.nexus.io.provided

import io.nats.client.*
import io.nats.client.impl.Headers
import io.netty.util.concurrent.DefaultThreadFactory
import kotlinx.serialization.*
import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.api.io.CommunicationProtocol
import me.santio.nexus.api.io.ReceivedPacket
import me.santio.nexus.api.nexus
import me.santio.nexus.data.encoding.BinaryEncoding
import me.santio.nexus.ext.async
import me.santio.nexus.io.Compression
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * Handles communication between nexus servers, each message will be scoped to a pool, allowing
 * for multiple server types to use a single nats cluster.
 * @author santio
 */
object NexusNats : CommunicationProtocol, MessageHandler {

    private val subscriptions = ConcurrentHashMap<Class<out NexusPacket>, MutableList<Consumer<*>>>()

    private var pool = "my-server"
    private var connection: Connection? = null
    private var dispatcher: Dispatcher? = null

    override fun connect(config: FileConfiguration) {
        this.pool = config.getString("pool.name", "my-server")!!
        val url = config.getString("nats.url", "nats://localhost:4222")!!

        require(!this.pool.contains(".")) { "Pool name cannot contain a period" }

        try {
            this.connection = Nats.connectReconnectOnConnect(Options.builder()
                .apply { url.split(",").map { it.trim() }.forEach { server(it) } }
                .connectionName("Nexus - ${nexus.id()}")
                .executor(ThreadPoolExecutor(0, Integer.MAX_VALUE, 500L, TimeUnit.MILLISECONDS,
                    SynchronousQueue(), DefaultThreadFactory("nexus-nats")))
                .build())

            this.dispatcher = this.connection!!.createDispatcher(this)
                .subscribe("nexus.$pool.global")
                .subscribe("nexus.$pool.${nexus.id()}")

            plugin.logger.info("Connected to NATS, Nexus will begin clustering (running as node ${nexus.id()})")
        } catch (e: IOException) {
            plugin.logger.severe("Failed to connect to cluster: $e")
            Bukkit.shutdown()
        } catch (e: InterruptedException) {
            plugin.logger.severe("Failed to connect to cluster: $e")
            Bukkit.shutdown()
        }
    }

    override fun disconnect() {
        try {
            this.connection?.close()
            this.dispatcher = null
            this.connection = null
        } catch (e: InterruptedException) {
            plugin.logger.severe("Failed to close connection: $e")
        }
    }

    /**
     * Subscribe to a certain nexus event
     * @param packet The event class to subscribe to
     * @param callback The callback that's called once this event is triggered
     */
    override fun <T : @Contextual NexusPacket> subscribe(packet: Class<out T>, callback: Consumer<ReceivedPacket<T>>) {
        this.subscriptions.getOrPut(packet) { ArrayList<Consumer<*>>() }.add(callback)
    }

    /**
     * Call the listeners notifying them an event was called
     */
    private fun <T : @Contextual NexusPacket> call(node: String, packet: T) = async {
        val subscribers = this.subscriptions.get(packet::class.java) ?: return@async

        subscribers
            .filterIsInstance<Consumer<ReceivedPacket<T>>>()
            .forEach { subscriber ->
                runCatching {
                    subscriber.accept(ReceivedPacket(packet, node))
                }.getOrElse { error ->
                    plugin.logger.severe("Failed to handle packet call for packet ${packet::class.simpleName}: $error")
                    error.printStackTrace()
                }
            }
    }

    /**
     * Publishes an event to the NATs cluster
     * @param packet The event to publish
     * @param target The target to publish to, or "global" for all nodes
     * @param receive Whether to manually call the subscriptions on this node
     * @param <T> The type of the event
     */
    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    override fun <T : @Contextual NexusPacket> publish(packet: T, target: String, receive: Boolean) {
        val connection = this.connection ?: return

        plugin.executor.execute {
            val packetData = BinaryEncoding.encode(
                packet::class.serializer() as SerializationStrategy<T>,
                packet
            )

            val headers = buildHeaders {
                add("packet", packet::class.java.name)
                add("node", nexus.id())
            }

            val compressed = Compression.compress(packetData)

//           NexusPlugin.instance().analytics().push(Metric.DECOMPRESSED_NET_TX, packetData.size)
//           NexusPlugin.instance().analytics().push(Metric.COMPRESSED_NET_TX, compressed.size)

            connection.publish("nexus.$pool.$target", headers, compressed)
            if (receive) this.call(nexus.id(), packet)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    override fun onMessage(msg: Message) = async {
        try {
            val headers = msg.headers ?: return@async
            val node = headers.getFirst("node")

            if (nexus.id() == node) return@async

            val compressedSize = msg.data.size
            val data = Compression.decompress(msg.data)

//            NexusPlugin.instance().analytics().push(Metric.COMPRESSED_NET_RX, compressedSize)
//            NexusPlugin.instance().analytics().push(Metric.DECOMPRESSED_NET_RX, data.size)

            val clazz = runCatching {
                Class.forName(headers.getFirst("packet"))
            }.getOrElse {
                val packet = headers.getFirst("packet") ?: "<unspecified>"
                plugin.logger.warning("Unknown packet received by node $node (packet: $packet)")
                return@async
            }

            val deserializer = clazz.kotlin.serializer() as KSerializer<out NexusPacket>
            val packet = BinaryEncoding.decode(deserializer, data)

            this.call(node, packet)
        } catch (e: Exception) {
            // NATS minifies our exception, this expands the trace
            plugin.logger.severe("An error occurred while attempting to call packet subscribers: $e")
            e.printStackTrace()
        }
    }

    private fun buildHeaders(block: Headers.() -> Unit): Headers = Headers().apply(block)
}
