package me.santio.nexus.virtual.entity

import com.github.retrooper.packetevents.protocol.entity.type.EntityType
import com.github.retrooper.packetevents.protocol.world.damagetype.DamageTypes
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation.EntityAnimationType
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import io.papermc.paper.registry.PaperSimpleRegistry
import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.entity.VirtualEntity
import me.santio.nexus.api.entity.VirtualEntity.Companion.ATTACK_RANGE
import me.santio.nexus.api.entity.VirtualEntity.Companion.ENTITY_RANGE
import me.santio.nexus.api.entity.VirtualEntity.Companion.MAX_WALK_DISTANCE
import me.santio.nexus.api.entity.data.Hand
import me.santio.nexus.data.Location
import me.santio.nexus.data.models.DisplayInventory
import me.santio.nexus.ext.async
import me.santio.nexus.ext.reflect
import me.santio.nexus.ext.sendPacket
import me.santio.nexus.ext.sync
import me.santio.nexus.virtual.packet.MetadataBuilder
import me.santio.nexus.virtual.packet.VirtualEntityPacket
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.inventory.EntityEquipment
import org.bukkit.util.Vector
import java.util.*
import java.util.Collections.emptyList
import kotlin.math.abs

/**
 * A virtual entity is a packet-based entity that exists on another node
 * @author santio
 */
abstract class PEVirtualEntity<C : CraftEntity>(
    override val node: String,
    override val uniqueId: UUID,
    open val entityType: EntityType
): VirtualEntity {

    override val entityId = SpigotReflectionUtil.generateEntityId()
    override val viewers = HashSet<UUID>()
    override var onGround: Boolean = true

    override var sprinting: Boolean = false
        set(value) {
            field = value
            this.updateMetadata()
        }

    override var pose: Pose = Pose.STANDING
        set(value) {
            field = value
            this.updateMetadata()
        }

    override var usingHand: Hand? = null
        set(value) {
            field = value
            this.updateMetadata()
        }

    override var location: Location? = null
    var lastMovement: Location? = null; private set
    var equipment: DisplayInventory? = null; private set

    override fun spawn(viewer: Player, data: Int, velocity: Vector?) {
        checkNotNull(this.location) { "Can't spawn in virtual entity without a location" }

        viewer.sendPacket(VirtualEntityPacket.createSpawnPacket(
            this.entityId,
            this.uniqueId,
            this.entityType,
            this.location!!,
            data,
            velocity
        ))

        viewer.sendPacket(VirtualEntityPacket.createTeleportPacket(
            this.entityId,
            this.location!!,
            this.onGround
        ))

        viewer.sendPacket(VirtualEntityPacket.createHeadRotPacket(
            this.entityId,
            this.location!!.yaw
        ))

        viewer.sendPacket(VirtualEntityPacket.createEntityMetadataPacket(
            this.entityId,
            this.createMetadata(viewer)
        ))

        if (this.equipment != null) {
            viewer.sendPacket(VirtualEntityPacket.createEntityEquipmentPacket(
                this.entityId,
                this.equipment
            ))
        }
    }

    /**
     * Create the additional entity metadata for this entity
     * @param viewer The player viewing the entity
     * @return A nullable entity metadata packet
     */
    abstract fun createMetadata(viewer: Player): MetadataBuilder

    override fun updateMetadata() {
        for (player in this.nearby()) {
            val builder = this.createMetadata(player)
            player.sendPacket(VirtualEntityPacket.createEntityMetadataPacket(entityId, builder))
        }
    }

    override fun remove(viewer: Player) {
        viewer.sendPacket(VirtualEntityPacket.createRemoveEntityPacket(entityId))
    }

    override fun walk(delta: Location) {
        if (lastMovement == null) lastMovement = delta

        val location = this.location ?: return
        val deltaHead = abs(delta.yaw - location.yaw) > 0.0001
            || abs(delta.pitch - location.pitch) > 0.0001

        location.world?.let { delta.world = it }
        location.add(delta).apply {
            yaw += delta.yaw
            pitch += delta.pitch
        }

        if (lastMovement!!.distanceSquared(delta) > MAX_WALK_DISTANCE * MAX_WALK_DISTANCE) {
            this.teleport(location)
        } else {
            this.nearby().sendPacket(VirtualEntityPacket.createMovePacket(
                this.entityId,
                delta,
                location,
                onGround
            ))

            if (deltaHead) {
                this.nearby().sendPacket(VirtualEntityPacket.createHeadRotPacket(
                    this.entityId,
                    location.yaw
                ))
            }
        }

        lastMovement = delta
    }

    override fun teleport(location: Location) {
        this.location = location

        this.nearby().sendPacket(VirtualEntityPacket.createTeleportPacket(
            this.entityId,
            location,
            false
        ))

        this.nearby().sendPacket(VirtualEntityPacket.createHeadRotPacket(
            this.entityId,
            location.yaw
        ))
    }

    override fun swingHand(hand: Hand) {
        val animationType = when (hand) {
            Hand.MAIN_HAND -> EntityAnimationType.SWING_MAIN_ARM
            Hand.OFF_HAND -> EntityAnimationType.SWING_OFF_HAND
        }

        this.nearby().sendPacket(VirtualEntityPacket.createEntityAnimationPacket(
            this.entityId,
            animationType
        ))
    }

    fun setEquipment(equipment: DisplayInventory?) {
        this.equipment = equipment

        this.nearby().sendPacket(VirtualEntityPacket.createEntityEquipmentPacket(
            this.entityId,
            equipment
        ))

        if (usingHand != null) async(delay = 1) { this.updateMetadata() }
    }

    override fun setEquipment(equipment: EntityEquipment?) {
        setEquipment(equipment?.let { DisplayInventory(it) })
    }

    override fun damaged(location: Vector, attacker: Int) {
        if (location.distanceSquared(this.location!!.toVector()) > ATTACK_RANGE * ATTACK_RANGE) return

        sync {
            val location = this.location ?: return@sync

            val hurtSound = PaperSimpleRegistry.SOUNDS.get(
                NamespacedKey.minecraft("entity." + this.entityType.name.getKey() + ".hurt")
            ) ?: run {
                return@sync plugin.logger.warning("Failed to get the hurt sound for $entityType")
            }

            location.world.playSound(
                location,
                hurtSound,
                1.0f, 1.0f
            )
        }

        this.nearby().sendPacket(VirtualEntityPacket.createEntityDamagePacket(
            this.entityId,
            DamageTypes.PLAYER_ATTACK,
            attacker,
            location
        ))
    }

    /**
     * Mimics a realistic attack by temporarily making a new NMS entity and attacking as if it
     * was a real entity on the server
     * @param entity The entity to attack
     */
    override fun attack(entity: Entity) {
        val craftEntity = this.createCraftEntity(entity.world)
        if (craftEntity !is CraftLivingEntity) {
            plugin.logger.warning("${this.entityType.name} tried to attack, except this entity can't attack")
            return
        }

        craftEntity.attack(entity)
    }

    protected abstract fun initializeCraftEntity(world: World): C

    /**
     * Create the closest 1:1 replica of a new craft entity as possible which matches the real entity on
     * the node it belongs on without it being ticked. These entities shouldn't persist and rather be
     * temporary use only, we don't want the node to start handling and ticking these entities.
     *
     * @param world The world to create the entity in
     * @return The new craft entity
     */
    open fun createCraftEntity(world: World?): C? {
        val location = location ?: return null
        val effectiveWorld = world ?: Bukkit.getWorlds().first()
        val craftEntity = this.initializeCraftEntity(effectiveWorld)
        val reflected = craftEntity.handle::class.reflect(craftEntity.handle)

        if (craftEntity is CraftLivingEntity) {
            craftEntity.equipment?.let { this.equipment?.apply(it) }
            craftEntity.handle.setOnGround(this.onGround)
            craftEntity.handle.isSprinting = this.sprinting
            craftEntity.handle.setPos(location.x, location.y, location.z)
            craftEntity.handle.setRot(location.yaw, location.pitch)

            reflected.call("detectEquipmentUpdates", "detectEquipmentUpdatesPublic")
        }

        return craftEntity
    }

    override fun nearby(): MutableList<out Player> {
        val location = this.location ?: return emptyList()

        return Bukkit.getOnlinePlayers().stream()
            .filter { it.location.distanceSquared(location) <= ENTITY_RANGE * ENTITY_RANGE }
            .toList()
    }
}
