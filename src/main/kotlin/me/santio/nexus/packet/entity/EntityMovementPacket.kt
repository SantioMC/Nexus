package me.santio.nexus.packet.entity

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.data.Location
import me.santio.nexus.data.UUID

@Serializable
data class EntityMovementPacket(
    val uniqueId: UUID,
    val delta: Location
) : NexusPacket