package me.santio.nexus.listener.bukkit.entity

import com.google.auto.service.AutoService
import io.papermc.paper.event.entity.EntityMoveEvent
import me.santio.nexus.api.entity.VirtualEntity
import me.santio.nexus.api.nexus
import me.santio.nexus.ext.async
import me.santio.nexus.ext.diff
import me.santio.nexus.packet.entity.EntityMovementPacket
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTeleportEvent

@AutoService(Listener::class)
class EntityMovementListener: Listener {

    // todo: optimize this even more, probably switch to a task to reduce context switching
    private fun onEntityMoved(entity: Entity, diff: Location) {
        if (entity.persistentDataContainer.has(nexus.entityNodeKey)) return

        val distanceSquared = Bukkit.getOnlinePlayers()
            .minOfOrNull { player -> player.location.distanceSquared(entity.location) }
            ?: -1.0

        if (distanceSquared > VirtualEntity.ENTITY_RANGE * VirtualEntity.ENTITY_RANGE) return
        nexus.cluster().publish(EntityMovementPacket(entity.uniqueId, diff))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onEntityMovement(event: EntityMoveEvent) = async {
        if (event.entity.persistentDataContainer.has(nexus.entityNodeKey)) return@async
        this.onEntityMoved(event.entity, event.to.diff(event.from))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onEntityTeleport(event: EntityTeleportEvent) = async {
        if (event.entity.persistentDataContainer.has(nexus.entityNodeKey)) return@async
        val to = event.to ?: return@async // Why is this nullable?
        this.onEntityMoved(event.entity, to.diff(event.from))
    }

}