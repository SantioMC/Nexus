package me.santio.nexus.packet.entity

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.data.UUID
import me.santio.nexus.data.Vector

@Serializable
data class EntityTookDamagePacket(
    val uniqueId: UUID,
    val location: Vector,
    val attacker: Int
) : NexusPacket