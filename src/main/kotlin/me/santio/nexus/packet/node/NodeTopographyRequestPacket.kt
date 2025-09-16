package me.santio.nexus.packet.node

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket

/**
 * This event is emitted when the node wants to receive a [NodeTopographyPacket] from all nodes in the cluster
 */
@Serializable
class NodeTopographyRequestPacket : NexusPacket