package me.santio.nexus.tasks

import me.santio.nexus.api.entity.VirtualEntity
import me.santio.nexus.api.nexus
import me.santio.nexus.data.player.PlayerTickerManager
import me.santio.nexus.ext.sync
import me.santio.nexus.virtual.entity.PEVirtualEntity
import org.bukkit.Bukkit

object VirtualEntityTicker : Runnable {
    override fun run() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return

        nexus.virtualization().virtualEntities.filterIsInstance<PEVirtualEntity<*>>().forEach { entity ->
            val location = entity.location ?: return@forEach

            val chunkX = location.chunk.x
            val chunkZ = location.chunk.z

            Bukkit.getOnlinePlayers().forEach { player ->
                val isTicking = PlayerTickerManager.isTicking(player, chunkX, chunkZ)
                val isViewer = entity.viewers.contains(player.uniqueId)
                val inRange = (player.location.distanceSquared(location)
                    <= VirtualEntity.ENTITY_RANGE * VirtualEntity.ENTITY_RANGE) && isTicking

                // Show/hide entity from player
                if (!isViewer && inRange) {
                    entity.viewers.add(player.uniqueId)
                    sync(delay = 1) { entity.spawn(player) }
                } else if (isViewer && !inRange) {
                    entity.remove(player)
                    entity.viewers.remove(player.uniqueId)
                }
            }
        }
    }
}
