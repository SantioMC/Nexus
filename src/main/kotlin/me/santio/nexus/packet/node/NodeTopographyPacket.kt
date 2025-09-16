package me.santio.nexus.packet.node

import kotlinx.serialization.Serializable
import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.packet.entity.EntityNonVirtualizedSpawnPacket
import me.santio.nexus.packet.entity.EntitySpawnPacket
import me.santio.nexus.packet.factory.EntityPacketFactory
import me.santio.nexus.packet.player.PlayerConnectPacket

/**
 * This event is emitted when a node is requesting current network topography, this will
 * hold the list of events that the node requesting needs to process to be brought up to date
 * with the current network topography
 */
@Serializable
data class NodeTopographyPacket(
    val heartbeat: NodeHeartbeatPacket,
    val onlineAt: Long,
    val players: List<PlayerConnectPacket>,
    val entities: List<EntitySpawnPacket>,
    val virtualized: List<EntityNonVirtualizedSpawnPacket>,
) : NexusPacket {
    companion object {
        @JvmStatic
        fun create(): NodeTopographyPacket {
            val entityPackets = plugin.server.worlds
                .flatMap { it.entities }
                .map { entity -> EntityPacketFactory.createSpawnPacket(entity) }

            return NodeTopographyPacket(
                NodeHeartbeatPacket.create(),
                plugin.onlineAt,
                plugin.server.onlinePlayers.map { player -> PlayerConnectPacket(player) }.toList(),
                entityPackets.filterIsInstance<EntitySpawnPacket>(),
                entityPackets.filterIsInstance<EntityNonVirtualizedSpawnPacket>()
            )
        }
    }
}
