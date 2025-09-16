package me.santio.nexus.packet.world

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.data.CompoundTag
import org.bukkit.Chunk

@Serializable
data class WorldChunkSyncPacket(
    val world: String,
    private val chunk: Long,
    val data: CompoundTag
): NexusPacket {

    constructor(world: String, chunkX: Int, chunkZ: Int, data: CompoundTag): this(world, Chunk.getChunkKey(chunkX, chunkZ), data)

    val chunkX = chunk.toInt()
    val chunkZ = (chunk shr 32).toInt()

}
