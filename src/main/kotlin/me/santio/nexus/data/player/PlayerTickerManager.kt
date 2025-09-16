package me.santio.nexus.data.player

import me.santio.nexus.api.nexus
import me.santio.nexus.virtual.entity.PEVirtualEntity
import org.bukkit.entity.Player
import java.util.*

/**
 * Handles information regarding ticking to players
 * @author santio
 */
object PlayerTickerManager {
    private val exclusions = HashSet<UUID>()
    private val chunksLoaded = HashMap<UUID, MutableSet<ChunkCoords>>()

    /**
     * Check if this player should be receiving ticking packets
     * @param player The player to check
     * @param chunkX The chunk x-coordinate to check
     * @param chunkZ The chunk z-coordinate to check
     * @return Whether this player should be receiving client-sided entity packets
     */
    fun isTicking(player: Player, chunkX: Int, chunkZ: Int): Boolean {
        if (this.exclusions.contains(player.uniqueId)) return false
        if (!this.chunksLoaded.containsKey(player.uniqueId)) return false

        return player.ticksLived > 20
            && this.chunksLoaded.get(player.uniqueId)!!.contains(ChunkCoords(chunkX, chunkZ))
    }

    /**
     * Exclude the provided player from being ticked entity packets
     * @param player The player to exclude
     */
    fun exclude(player: Player) {
        this.exclusions.add(player.uniqueId)
    }

    /**
     * Add the provided player back to receiving packets
     * @param player The player to unexclude
     */
    fun unexclude(player: Player) {
        this.exclusions.remove(player.uniqueId)
    }

    /**
     * Resets all data on the specified player. This will also remove the player as
     * a viewer from all existing virtual entities.
     * @param player The player to reset the data of
     */
    fun reset(player: Player) {
        this.exclusions.remove(player.uniqueId)
        this.chunksLoaded.remove(player.uniqueId)

        nexus.virtualization().virtualEntities
            .filterIsInstance<PEVirtualEntity<*>>()
            .forEach { entity ->
                entity.viewers.remove(player.uniqueId)
            }
    }

    /**
     * Mark a chunk as loaded for the player, this allows virtual entities to be ticked in that chunk
     * @param player The player that the chunk is being shown to
     * @param chunkX The chunk x-coordinate that was loaded in
     * @param chunkZ The chunk z-coordinate that was loaded in
     */
    fun chunkLoaded(player: Player, chunkX: Int, chunkZ: Int) {
        val chunksLoaded = this.chunksLoaded.getOrDefault(player.uniqueId, HashSet<ChunkCoords>())
        val chunkIdentifier = ChunkCoords(chunkX, chunkZ)

        chunksLoaded.add(chunkIdentifier)
        this.chunksLoaded.put(player.uniqueId, chunksLoaded)
    }

    /**
     * Mark a chunk as unloaded for the player, this allows virtual entities to stop being ticked in that chunk
     * @param player The player that the chunk is being shown to
     * @param chunkX The chunk x-coordinate that was loaded in
     * @param chunkZ The chunk z-coordinate that was loaded in
     */
    fun chunkUnloaded(player: Player, chunkX: Int, chunkZ: Int) {
        val chunksLoaded = this.chunksLoaded.getOrDefault(player.uniqueId, HashSet<ChunkCoords>())
        val chunkIdentifier = ChunkCoords(chunkX, chunkZ)

        chunksLoaded.remove(chunkIdentifier)
        this.chunksLoaded.put(player.uniqueId, chunksLoaded)
    }

    @JvmRecord
    private data class ChunkCoords(val chunkX: Int, val chunkZ: Int)
}
