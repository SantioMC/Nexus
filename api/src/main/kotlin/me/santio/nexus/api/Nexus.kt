package me.santio.nexus.api

import me.santio.nexus.api.entity.Virtualization
import me.santio.nexus.api.io.CommunicationProtocol
import me.santio.nexus.api.node.Node
import me.santio.nexus.api.node.NodeManager
import me.santio.nexus.api.player.NexusPlayer
import me.santio.nexus.api.player.PlayerManager
import me.santio.nexus.api.world.WorldSynchronizer
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*

/**
 * The primary entrypoint to the Nexus API.
 *
 * The API is used for external plugins and dependencies interacting with Nexus itself,
 * before interacting with Nexus you should first check if the plugin is actually
 * running on the server.
 *
 * ```kotlin
 * val enabled = Bukkit.getPluginManager().isPluginEnabled("Nexus")
 * ```
 *
 * To retrieve the Nexus API instance, you should use the Bukkit service manager.
 * ```kotlin
 * val nexus = Bukkit.getServicesManager().load(Nexus::class.java)
 * ```
 *
 * As a quick reminder, this will throw an exception if you do not first check if Nexus is enabled.
 *
 * Nexus will attempt to synchronize state between servers as well as possible, however that isn't always possible.
 * The Bukkit server is also completely unaware of players or entities on other nodes, instead Nexus holds all of that
 * information.
 *
 * While Nexus is written in Kotlin, it attempts to provide it's public-facing API in a compatible way with Java to
 * better support other plugins, however some features are impossible to support, such as custom packets (see [NexusPacket])
 *
 * @author santio
 */
interface Nexus {

    /**
     * The identifier for the current node, this can be used to uniquely identify the server within the cluster. This
     * identifier should be persistent and stored alongside the server, allowing it to be a fixed value across restarts.
     */
    fun id(): String

    /**
     * The cluster is the core communication layer for Nexus, used for sending and receiving [NexusPacket]s. Most
     * interactions with the cluster are ran asynchronously and are safe to call regardless of the thread you're on.
     *
     * @return The current communication protocol
     */
    fun cluster(): CommunicationProtocol

    /**
     * The virtualization layer is how Nexus will fake entities between servers, by creating entities completely
     * out of packets. This results in a very minimal hit to performance while realistically only consuming memory
     * (for the tracking of all entities)
     *
     * @return The current virtualization protocol
     */
    fun virtualization(): Virtualization

    /**
     * The node manager is in charge of keeping a reference to every node connected to the cluster, and manage
     * the master node.
     *
     * @return The current node manager implementation
     */
    fun nodes(): NodeManager

    /**
     * Get the current node that represents the caller node
     *
     * @return The node representing the caller server, or null if not yet ready
     */
    fun self(): Node?

    /**
     * The player manager is responsible for keeping track of all players across all nodes
     *
     * @return The current player manager implementation
     */
    fun players(): PlayerManager

    /**
     * Retrieve the specified [NexusPlayer] from the provided [Player]
     *
     * @param player The bukkit player
     * @return The nexus player data
     *
     * @throws IllegalStateException if the player is no longer online when called
     */
    fun player(player: Player): NexusPlayer

    /**
     * Retrieve the specified [NexusPlayer] from the provided [UUID]
     *
     * @param uniqueId The unique identifier of the player
     * @return The nexus player, if they're currently connected to the cluster
     */
    fun player(uniqueId: UUID): NexusPlayer?

    /**
     * The configuration that Nexus is running with
     *
     * @return The configuration details for Nexus
     */
    fun config(): NexusConfig

    /**
     * Get the world synchronizer which manages synchronizing data between worlds, interacting with the
     * synchronizer is considered dangerous and may occur data loss.
     *
     * @param world The name of the world to get the synchronizer for
     * @return The current implementation for the world synchronization
     */
    fun world(world: String): WorldSynchronizer

    /**
     * Get the world synchronizer which manages synchronizing data between worlds, interacting with the
     * synchronizer is considered dangerous and may occur data loss.
     *
     * @param world The world to get the synchronizer for
     * @return The current implementation for the world synchronization
     */
    fun world(world: World): WorldSynchronizer

    /**
     * This is an NBT key added to entities which exist on all servers, but are managed by Nexus. While most
     * entities on Nexus are virtualized (packet-based), some can't be. For example, item drops or XP orbs are some
     * examples of non-virtualized entities that are synchronized between servers.
     */
    val entityNodeKey: NamespacedKey

}