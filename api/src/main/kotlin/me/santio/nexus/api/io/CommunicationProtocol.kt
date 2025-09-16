package me.santio.nexus.api.io

import kotlinx.serialization.Contextual
import me.santio.nexus.api.NexusPacket
import org.bukkit.configuration.file.FileConfiguration
import java.util.function.Consumer

/**
 * A protocol for communicating between nodes on the cluster.
 * @author santio
 */
interface CommunicationProtocol {
    /**
     * Establish a connection to the cluster
     * @param config The configuration to use to connect
     */
    fun connect(config: FileConfiguration)

    /**
     * Close the connection to the cluster
     */
    fun disconnect()

    /**
     * Subscribe to a certain nexus event
     * @param packet The event class to subscribe to
     * @param callback The callback that's called once this event is triggered
     * @param <T> The type of the event
     */
    fun <T : @Contextual NexusPacket> subscribe(packet: Class<out T>, callback: Consumer<ReceivedPacket<T>>)

    /**
     * Publishes an event to the NATs cluster
     * @param packet The event to publish
     * @param target The target to publish to, or "global" for all nodes
     * @param receive Whether to manually call the subscriptions on this node
     * @param <T> The type of the event
     */
    fun <T : @Contextual NexusPacket> publish(packet: T, target: String = "global", receive: Boolean = false)

}

inline fun <reified T : @Contextual NexusPacket> CommunicationProtocol.subscribe(crossinline callback: (ReceivedPacket<T>) -> Unit) =
    subscribe(T::class.java) { callback(it) }
