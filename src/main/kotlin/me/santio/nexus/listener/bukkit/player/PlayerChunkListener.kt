package me.santio.nexus.listener.bukkit.player

import com.google.auto.service.AutoService
import io.papermc.paper.event.packet.PlayerChunkLoadEvent
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent
import me.santio.nexus.data.player.PlayerTickerManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@AutoService(Listener::class)
class PlayerChunkListener: Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerLoadChunk(event: PlayerChunkLoadEvent) {
        PlayerTickerManager.chunkLoaded(
            event.player,
            event.getChunk().x,
            event.getChunk().z
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerUnloadChunk(event: PlayerChunkUnloadEvent) {
        PlayerTickerManager.chunkUnloaded(
            event.player,
            event.getChunk().x,
            event.getChunk().z
        )
    }

}