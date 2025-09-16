package me.santio.nexus.listener.bukkit.entity

import com.google.auto.service.AutoService
import me.santio.nexus.api.nexus
import me.santio.nexus.data.node.NodeImpl
import me.santio.nexus.event.AsyncChunkLoadEvent
import me.santio.nexus.event.AsyncChunkUnloadEvent
import me.santio.nexus.ext.sync
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRemoveEvent
import org.bukkit.event.entity.EntitySpawnEvent

@AutoService(Listener::class)
class EntityStateListener: Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onEntitySpawn(event: EntitySpawnEvent) {
        nexus.self()?.let { it as NodeImpl }?.entities?.add(event.entity.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onEntityRemove(event: EntityRemoveEvent) {
        nexus.self()?.let { it as NodeImpl }?.entities?.remove(event.entity.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onChunkLoad(event: AsyncChunkLoadEvent) {
        val entities = event.chunk.entities
        val toRemove = entities.filter { it.persistentDataContainer.has(nexus.entityNodeKey) }

        sync { toRemove.forEach { it.remove() } }

        val node = nexus.self()?.let { it as NodeImpl } ?: return
        node.entities.addAll(event.chunk.entities.filterNot { it in toRemove }.map { it.uniqueId })
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onChunkUnload(event: AsyncChunkUnloadEvent) {
        val node = nexus.self()?.let { it as NodeImpl } ?: return
        node.entities.removeAll(event.chunk.entities.map { it.uniqueId }.toSet())
    }

}