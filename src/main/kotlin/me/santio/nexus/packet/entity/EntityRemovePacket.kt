package me.santio.nexus.packet.entity

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.data.UUID

@Serializable
data class EntityRemovePacket(
    val uniqueId: UUID
) : NexusPacket