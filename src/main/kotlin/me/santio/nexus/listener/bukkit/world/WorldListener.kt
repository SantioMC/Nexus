package me.santio.nexus.listener.bukkit.world

import com.google.auto.service.AutoService
import me.santio.nexus.api.nexus
import me.santio.nexus.ext.async
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkPopulateEvent
import org.bukkit.event.world.WorldSaveEvent

@AutoService(Listener::class)
class WorldListener: Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private fun onChunkPopulate(event: ChunkPopulateEvent) = async(delay = 2) {
        // If the chunk is newly generated, we'll push it to the other nodes
        nexus.world(event.world).pushChunk(event.chunk.x, event.chunk.z)
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private fun onWorldSave(event: WorldSaveEvent) = async {
        nexus.world(event.world).save()
    }

}
