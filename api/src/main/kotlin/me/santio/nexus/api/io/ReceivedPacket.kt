package me.santio.nexus.api.io

import me.santio.nexus.api.NexusPacket

/**
 * Represents a packet received by another node, which holds the incoming data but also
 * the node that the data originated from
 *
 * @author santio
 */
data class ReceivedPacket<T : NexusPacket>(
    val event: T,
    val node: String,
)
