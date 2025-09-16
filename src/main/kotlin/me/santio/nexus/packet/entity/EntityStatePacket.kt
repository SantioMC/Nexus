package me.santio.nexus.packet.entity

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.data.UUID
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

@Serializable
data class EntityStatePacket(
    val uniqueId: UUID,
    val onGround: Boolean,
    val sprinting: Boolean,
    val onFire: Boolean,
    val swimming: Boolean,
    val elytra: Boolean,
    val glowing: Boolean,
    val invisible: Boolean,
) : NexusPacket {
    companion object {
        @JvmStatic
        fun of(entity: Entity): EntityStatePacket {
            return EntityStatePacket(
                entity.uniqueId,
                entity.isOnGround,
                entity is Player && entity.isSprinting,
                entity.isVisualFire || entity.fireTicks > 0,
                entity is Player && entity.isSwimming,
                entity is LivingEntity && entity.isGliding,
                entity.isGlowing,
                entity.isInvisible
            )
        }
    }
}
