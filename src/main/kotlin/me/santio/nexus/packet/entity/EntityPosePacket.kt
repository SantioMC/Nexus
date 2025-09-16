package me.santio.nexus.packet.entity

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.data.UUID
import org.bukkit.entity.Pose

@Serializable
data class EntityPosePacket(
    val uniqueId: UUID,
    val pose: Pose
) : NexusPacket
