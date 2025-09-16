package me.santio.nexus.packet.player

import com.github.retrooper.packetevents.protocol.player.PublicProfileKey
import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.data.UUID
import me.santio.nexus.data.serializer.PublicProfileSerializer

@Serializable
data class PlayerChatSessionPacket(
    val uniqueId: UUID,
    val sessionId: UUID,
    @Serializable(with = PublicProfileSerializer::class)
    val publicProfileKey: PublicProfileKey
) : NexusPacket
