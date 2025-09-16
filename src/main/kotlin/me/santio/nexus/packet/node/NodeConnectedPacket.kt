package me.santio.nexus.packet.node

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket

/**
 * This event is emitted when a node is connected to the pool
 */
@Serializable
data class NodeConnectedPacket(
    val onlineAt: Long
): NexusPacket
