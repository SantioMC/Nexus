package me.santio.nexus.packet.player

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.data.UUID

@Serializable
data class PlayerRiptidePacket(
    val uniqueId: UUID,
    val isRiptiding: Boolean,
) : NexusPacket
