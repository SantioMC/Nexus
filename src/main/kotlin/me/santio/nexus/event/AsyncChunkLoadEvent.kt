package me.santio.nexus.event

import org.bukkit.Chunk
import org.bukkit.World

data class AsyncChunkLoadEvent(
    val world: World,
    val chunk: Chunk
): NexusEvent()
