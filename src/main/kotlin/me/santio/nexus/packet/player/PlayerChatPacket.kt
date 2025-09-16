package me.santio.nexus.packet.player

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket

@Serializable
data class PlayerChatPacket(
    val playerChatMessage: String
) : NexusPacket
