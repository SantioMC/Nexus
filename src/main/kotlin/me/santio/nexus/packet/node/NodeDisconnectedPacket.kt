package me.santio.nexus.packet.node

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket

/**
 * This event is emitted when a node is disconnected to the pool
 */
@Serializable
class NodeDisconnectedPacket: NexusPacket