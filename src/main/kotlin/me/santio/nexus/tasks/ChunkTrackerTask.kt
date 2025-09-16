package me.santio.nexus.tasks

import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.data.UUID
import me.santio.nexus.event.AsyncChunkLoadEvent
import me.santio.nexus.event.AsyncChunkUnloadEvent
import org.bukkit.Chunk

/**
 * Tracks when chunks are loaded and unloaded, allowing for less context switching compared to listening to the
 * Bukkit [org.bukkit.event.world.ChunkLoadEvent] event.
 *
 * @author santio
 */
object ChunkTrackerTask: Runnable {

    private val chunksLoaded: MutableMap<UUID, List<Long>> = mutableMapOf()

    override fun run() = plugin.server.worlds.forEach { world ->
        val chunks = world.loadedChunks
        val loaded = chunksLoaded.getOrPut(world.uid) { listOf() }
        val chunkKeys = chunks.map { Chunk.getChunkKey(it.x, it.z) }

        val new = chunks.filter { Chunk.getChunkKey(it.x, it.z) !in loaded }
        val old = loaded.filter { it !in chunkKeys }.map { world.getChunkAt(it) }

        val events = new.map { AsyncChunkLoadEvent(world, it) } +
            old.map { AsyncChunkUnloadEvent(world, it) }

        events.forEach { it.callEvent() }
        chunksLoaded[world.uid] = chunkKeys
    }

}