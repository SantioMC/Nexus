package me.santio.nexus.packet.node

import kotlinx.serialization.Serializable
import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.api.nexus
import java.time.Instant
import kotlin.math.round

@Serializable
data class NodeHeartbeatPacket(
    val tps: Float,
    val mspt: Float,
    val timeSent: Long,
    val maxPlayers: Int,
    val master: Boolean,
) : NexusPacket {
    companion object {
        @JvmStatic
        fun create(): NodeHeartbeatPacket {
            return NodeHeartbeatPacket(
                round(plugin.server.tps[0] * 100.0).toFloat() / 100,
                round(plugin.server.averageTickTime * 100.0).toFloat() / 100,
                Instant.now().toEpochMilli(),
                plugin.server.onlinePlayers.size,
                nexus.self()?.master ?: false,
            )
        }
    }
}
