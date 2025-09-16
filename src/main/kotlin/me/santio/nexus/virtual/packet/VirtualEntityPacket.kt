package me.santio.nexus.virtual.packet

import com.github.retrooper.packetevents.manager.server.ServerVersion
import com.github.retrooper.packetevents.protocol.entity.EntityPositionData
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.type.EntityType
import com.github.retrooper.packetevents.protocol.player.Equipment
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot
import com.github.retrooper.packetevents.protocol.world.damagetype.DamageType
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import com.github.retrooper.packetevents.wrapper.play.server.*
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.santio.nexus.data.Location
import me.santio.nexus.data.Vector
import me.santio.nexus.data.models.DisplayInventory
import me.santio.nexus.ext.packetevents
import org.bukkit.entity.Pose
import java.util.*
import kotlin.math.abs

/**
 * Utility class for making packets for virtual entities
 * @author santio
 */
internal object VirtualEntityPacket {
    fun createTeleportPacket(
        entityId: Int,
        location: Location,
        onGround: Boolean
    ): PacketWrapper<*> {
        return WrapperPlayServerEntityPositionSync(
            entityId,
            EntityPositionData(
                Vector3d(location.x, location.y, location.z),
                Vector3d(0.0, 0.0, 0.0),
                location.yaw,
                location.pitch
            ),
            onGround
        )
    }

    fun createSpawnPacket(
        entityId: Int,
        uniqueId: UUID,
        entityType: EntityType,
        location: Location,
        entityData: Int,
        velocity: Vector?
    ): PacketWrapper<*> {
        val version = packetevents.serverManager.version

        if (version.isNewerThanOrEquals(ServerVersion.V_1_20_2)) {
            return WrapperPlayServerSpawnEntity(
                entityId,
                Optional.of(uniqueId),
                entityType,
                Vector3d(location.x, location.y, location.z),
                location.pitch,
                location.yaw,
                location.yaw,
                entityData,
                Optional.ofNullable(velocity?.let { Vector3d(it.x, it.y, it.z) })
            )
        } else {
            return WrapperPlayServerSpawnPlayer(
                entityId,
                uniqueId,
                SpigotConversionUtil.fromBukkitLocation(location)
            )
        }
    }

    fun createRemoveEntityPacket(entityId: Int): PacketWrapper<*> {
        return WrapperPlayServerDestroyEntities(entityId)
    }

    fun createMovePacket(
        entityId: Int,
        diff: Location,
        to: Location,
        onGround: Boolean
    ): PacketWrapper<*>? {
        val deltaHead = abs(diff.yaw) > 0.001 || abs(diff.pitch) > 0.001
        val deltaXYZ = abs(diff.x) > 0.001 || abs(diff.y) > 0.001 || abs(diff.z) > 0.001

        if (deltaXYZ && deltaHead) {
            return WrapperPlayServerEntityRelativeMoveAndRotation(
                entityId,
                diff.x,
                diff.y,
                diff.z,
                to.yaw,
                to.pitch,
                onGround
            )
        } else if (deltaHead) {
            return WrapperPlayServerEntityRotation(
                entityId,
                to.yaw,
                to.pitch,
                onGround
            )
        } else if (deltaXYZ) {
            return WrapperPlayServerEntityRelativeMove(
                entityId,
                diff.x,
                diff.y,
                diff.z,
                onGround
            )
        } else return null
    }

    fun createHeadRotPacket(entityId: Int, headYaw: Float): PacketWrapper<*> {
        return WrapperPlayServerEntityHeadLook(
            entityId,
            headYaw
        )
    }

    fun createEntityAnimationPacket(
        entityId: Int,
        animationType: WrapperPlayServerEntityAnimation.EntityAnimationType
    ): PacketWrapper<*> {
        return WrapperPlayServerEntityAnimation(
            entityId,
            animationType
        )
    }

    fun createEntityEquipmentPacket(
        entityId: Int,
        inventory: DisplayInventory?
    ): PacketWrapper<*> {
        // todo: send diff only
        val equipment = inventory?.let {
            listOf(
                Equipment(EquipmentSlot.MAIN_HAND, SpigotConversionUtil.fromBukkitItemStack(it.mainHand)),
                Equipment(EquipmentSlot.OFF_HAND, SpigotConversionUtil.fromBukkitItemStack(it.offHand)),
                Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(it.helmet)),
                Equipment(EquipmentSlot.CHEST_PLATE, SpigotConversionUtil.fromBukkitItemStack(it.chestplate)),
                Equipment(EquipmentSlot.LEGGINGS, SpigotConversionUtil.fromBukkitItemStack(it.leggings)),
                Equipment(EquipmentSlot.BOOTS, SpigotConversionUtil.fromBukkitItemStack(it.boots))
            )
        } ?: emptyList()

        return WrapperPlayServerEntityEquipment(
            entityId,
            equipment
        )
    }

    fun createEntityPosePacket(entityId: Int, pose: Pose): PacketWrapper<*> {
        val pose = SpigotConversionUtil.fromBukkitPose(pose)
        return WrapperPlayServerEntityMetadata(
            entityId,
            mutableListOf<EntityData<*>>(EntityData(6, EntityDataTypes.ENTITY_POSE, pose))
        )
    }

    fun createEntityDamagePacket(
        entityId: Int,
        damageType: DamageType,
        attackerId: Int,
        sourcePosition: Vector
    ): PacketWrapper<*> {
        return WrapperPlayServerDamageEvent(
            entityId,
            damageType,
            attackerId,
            attackerId,
            Vector3d(sourcePosition.x, sourcePosition.y, sourcePosition.z)
        )
    }

    fun createEntityMetadataPacket(entityId: Int, builder: MetadataBuilder): PacketWrapper<*> {
        return WrapperPlayServerEntityMetadata(entityId, builder.build())
    }
}
