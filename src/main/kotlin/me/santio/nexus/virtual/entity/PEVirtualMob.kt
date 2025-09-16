package me.santio.nexus.virtual.entity

import com.github.retrooper.packetevents.protocol.entity.type.EntityType
import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.ext.reflect
import me.santio.nexus.virtual.packet.MetadataBuilder
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*

/**
 * A virtual mob is a non-player entity that exists on another node. This virtualized entity
 * is a bit misleading in name, as this may also include non-living entities and not just limited
 * to living mobs.
 *
 * @author santio
 */
class PEVirtualMob(
    node: String,
    uniqueId: UUID,
    override val entityType: EntityType,
    private val initialNbt: CompoundTag?
) : PEVirtualEntity<CraftEntity>(node, uniqueId, entityType) {

    private var nbt: CompoundTag? = initialNbt

    override fun spawn(viewer: Player, data: Int, velocity: Vector?) {
        val entity = this.createCraftEntity(null) ?: error("Failed to create craft entity")

        val entityData = when (this.entityType.name.getKey()) {
            "painting", "item_frame" -> this.directionToData(entity.facing)
            else -> 0
        }

        super.spawn(viewer, entityData, entity.velocity)
    }

    override fun createMetadata(viewer: Player): MetadataBuilder {
        this.sendEntityMetadata(viewer)
        return MetadataBuilder.empty()
    }

    /**
     * Entities have a lot of metadata, and this could possibly change wildly in the future, so we'll call
     * the built-in nms packet for syncing metadata
     * @param player The player to send the metadata to
     */
    private fun sendEntityMetadata(player: Player) {
        val entity = this.createCraftEntity(null) ?: return
        val entityData = Entity::class
            .reflect(entity.handle)
            .get<SynchedEntityData>("entityData")
            ?: return

        (player as CraftPlayer).handle.connection.sendPacket(
            ClientboundSetEntityDataPacket(this.entityId, entityData.packAll())
        )
    }

    override fun initializeCraftEntity(world: World): CraftEntity {
        val nmsEntityTypeOptional = net.minecraft.world.entity.EntityType
            .byString(this.entityType.name.toString())

        val nmsEntityType = nmsEntityTypeOptional.orElse(null)
        if (nmsEntityType == null) {
            plugin.logger.severe("Failed to create NMS entity: ${entityType.name}")
            throw IllegalStateException("Failed to find entity type from resource key")
        }

        val level: ServerLevel = (world as CraftWorld).handle
        val entity: Entity? = nmsEntityType.create(
            level,
            EntitySpawnReason.NATURAL
        )

        if (entity == null) {
            plugin.logger.severe("Creation of NMS entity ${entityType.name} failed, are entities enabled in world ${world.name}?",)
            throw IllegalStateException("Creation of NMS entity failed, are entities enabled in this world?")
        }

        if (this.initialNbt != null) {
            entity.load(this.initialNbt)
            entity.setUUID(this.uniqueId)
        }

        return entity.bukkitEntity
    }

    private fun directionToData(direction: BlockFace): Int {
        return when (direction) {
            BlockFace.UP -> 1
            BlockFace.NORTH -> 2
            BlockFace.SOUTH -> 3
            BlockFace.WEST -> 4
            BlockFace.EAST -> 5
            else -> 0
        }
    }
}
