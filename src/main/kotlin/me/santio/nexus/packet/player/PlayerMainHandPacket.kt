package me.santio.nexus.packet.player

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.data.UUID

@Serializable
data class PlayerMainHandPacket(
    val uniqueId: UUID,
    val isRightHand: Boolean
) : NexusPacket
