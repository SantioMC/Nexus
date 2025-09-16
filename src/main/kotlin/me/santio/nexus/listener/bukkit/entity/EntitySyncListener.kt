package me.santio.nexus.listener.bukkit.entity

import com.google.auto.service.AutoService
import me.santio.nexus.api.nexus
import me.santio.nexus.packet.entity.*
import me.santio.nexus.packet.factory.EntityPacketFactory
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.*

@AutoService(Listener::class)
class EntitySyncListener: Listener {

    @Suppress("UnstableApiUsage") // - We need it
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onEntityDamaged(event: EntityDamageEvent) {
        if (event.entity.persistentDataContainer.has(nexus.entityNodeKey)) return

        val damageSource = event.damageSource
        val attackerId = damageSource.causingEntity?.entityId ?: 0
        val attackedFrom = damageSource.sourceLocation ?: event.entity.location

        nexus.cluster().publish(EntityTookDamagePacket(
            event.entity.uniqueId,
            attackedFrom.toVector(),
            attackerId
        ))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onEntitySpawn(event: EntitySpawnEvent) {
        if (event.entity.persistentDataContainer.has(nexus.entityNodeKey)) return
        nexus.cluster().publish(EntityPacketFactory.createSpawnPacket(event.entity))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onEntityPose(event: EntityPoseChangeEvent) {
        if (event.entity.persistentDataContainer.has(nexus.entityNodeKey)) return
        nexus.cluster().publish(EntityPosePacket(event.entity.uniqueId, event.pose))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onEntityDespawn(event: EntityRemoveEvent) {
        if (event.entity.persistentDataContainer.has(nexus.entityNodeKey)) return
        nexus.cluster().publish(EntityRemovePacket(event.entity.uniqueId))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onEntityDeath(event: EntityDeathEvent) {
        if (event.entity.persistentDataContainer.has(nexus.entityNodeKey)) return
        nexus.cluster().publish(EntityDeathPacket(event.entity.uniqueId))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onEntityFire(event: EntityCombustEvent) {
        if (event.entity.persistentDataContainer.has(nexus.entityNodeKey)) return
        nexus.cluster().publish(EntityStatePacket.of(event.entity))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onItemPickup(event: EntityPickupItemEvent) {
        nexus.cluster().publish(EntityRemovePacket(event.item.uniqueId))
    }

}