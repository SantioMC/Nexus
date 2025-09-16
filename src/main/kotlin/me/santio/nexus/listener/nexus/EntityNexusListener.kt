package me.santio.nexus.listener.nexus

import com.google.auto.service.AutoService
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.santio.nexus.api.getVirtualEntity
import me.santio.nexus.api.io.ReceivedPacket
import me.santio.nexus.api.io.subscribe
import me.santio.nexus.api.nexus
import me.santio.nexus.data.UUID
import me.santio.nexus.data.node.NodeImpl
import me.santio.nexus.ext.sync
import me.santio.nexus.packet.entity.*
import me.santio.nexus.service.NexusListener
import me.santio.nexus.virtual.entity.PEVirtualEntity
import me.santio.nexus.virtual.entity.PEVirtualMob
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.entity.Pose
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.persistence.PersistentDataType

@AutoService(NexusListener::class)
class EntityNexusListener: NexusListener {
    override fun register() {
        nexus.cluster().subscribe<EntitySpawnPacket>(::handleEntitySpawn)
        nexus.cluster().subscribe<EntityNonVirtualizedSpawnPacket>(::handleNonVirtualizedSpawn)
        nexus.cluster().subscribe<EntityDeathPacket>(::handleEntityDeath)
        nexus.cluster().subscribe<EntityStatePacket>(::handleEntityState)
        nexus.cluster().subscribe<EntityHandAnimationPacket>(::handleHandAnimation)
        nexus.cluster().subscribe<EntityMovementPacket>(::handleEntityMovement)
        nexus.cluster().subscribe<EntityPosePacket>(::handleEntityPose)
        nexus.cluster().subscribe<EntityRemovePacket>(::handleEntityRemove)
        nexus.cluster().subscribe<EntityTookDamagePacket>(::handleEntityDamage)
        nexus.cluster().subscribe<EntityEquipmentPacket>(::handleEntityEquipment)
        nexus.cluster().subscribe<EntityUseHandPacket>(::handleEntityUseHand)
        nexus.cluster().subscribe<AttackEntityPacket>(::handleEntityAttack)
    }

    private fun entity(uniqueId: UUID): PEVirtualEntity<*>? {
        return nexus.virtualization().getVirtualEntity<PEVirtualEntity<*>>(uniqueId)
    }

    private fun handleEntitySpawn(packet: ReceivedPacket<EntitySpawnPacket>) {
        val virtualMob = PEVirtualMob(packet.node, packet.event.uniqueId, SpigotConversionUtil.fromBukkitEntityType(packet.event.entityType), packet.event.nbt)
        nexus.virtualization().createVirtualEntity(virtualMob, packet.event.location)
        nexus.nodes().get(packet.node)?.let { it as NodeImpl }?.entities?.add(packet.event.uniqueId)
    }

    private fun handleNonVirtualizedSpawn(packet: ReceivedPacket<EntityNonVirtualizedSpawnPacket>) = sync {
        nexus.nodes().get(packet.node)?.let { it as NodeImpl }?.entities?.add(packet.event.uniqueId)
        val location = packet.event.location
        location.world.spawnEntity(location, packet.event.entityType, CreatureSpawnEvent.SpawnReason.DEFAULT) {
            it.persistentDataContainer.set(nexus.entityNodeKey, PersistentDataType.STRING, packet.node)
            val nmsEntity = (it as CraftEntity).handle
            nmsEntity.load(packet.event.nbt)
            nmsEntity.setUUID(packet.event.uniqueId)
        }
    }

    private fun handleEntityDeath(packet: ReceivedPacket<EntityDeathPacket>) {
        entity(packet.event.uniqueId)?.pose = Pose.DYING
    }

    private fun handleEntityState(packet: ReceivedPacket<EntityStatePacket>) {
        entity(packet.event.uniqueId)?.let { entity ->
            entity.onGround = packet.event.onGround
            entity.sprinting = packet.event.sprinting
        }
    }

    private fun handleHandAnimation(packet: ReceivedPacket<EntityHandAnimationPacket>) {
        entity(packet.event.uniqueId)?.swingHand(packet.event.hand)
    }

    private fun handleEntityMovement(packet: ReceivedPacket<EntityMovementPacket>) {
        entity(packet.event.uniqueId)?.walk(packet.event.delta)
    }

    private fun handleEntityPose(packet: ReceivedPacket<EntityPosePacket>) {
        entity(packet.event.uniqueId)?.pose = packet.event.pose
    }

    private fun handleEntityEquipment(packet: ReceivedPacket<EntityEquipmentPacket>) {
        entity(packet.event.uniqueId)?.setEquipment(packet.event.inventory)
    }

    private fun handleEntityDamage(packet: ReceivedPacket<EntityTookDamagePacket>) {
        entity(packet.event.uniqueId)?.damaged(packet.event.location, packet.event.attacker)
    }

    private fun handleEntityUseHand(packet: ReceivedPacket<EntityUseHandPacket>) {
        entity(packet.event.uniqueId)?.usingHand = packet.event.hand
    }

    private fun handleEntityRemove(packet: ReceivedPacket<EntityRemovePacket>) {
        nexus.virtualization().removeVirtualEntity(packet.event.uniqueId)
        nexus.nodes().get(packet.node)?.let { it as NodeImpl }?.entities?.remove(packet.event.uniqueId)
        sync { Bukkit.getEntity(packet.event.uniqueId)?.remove() }
    }

    private fun handleEntityAttack(packet: ReceivedPacket<AttackEntityPacket>) {
        val attacker = entity(packet.event.attacker) ?: return
        val world = attacker.location?.world ?: return
        sync { world.getEntity(packet.event.uniqueId)?.let { attacker.attack(it) } }
    }

}