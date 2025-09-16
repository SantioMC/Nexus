package me.santio.nexus.api.player

import java.util.*

/**
 * The player manager is used to keep track of all players across the cluster
 *
 * @author santio
 */
interface PlayerManager {

    /**
     * Get the list of all NexusPlayers across the entire cluster
     * @return A collection of all players
     */
    fun all(): List<NexusPlayer>

    /**
     * Get the count of players across the entire cluster
     * @return The amount of players online
     */
    fun size(): Int

    /**
     * Get a cached NexusPlayer from the cluster by their unique identifier
     * @param uniqueId The unique identifier of the player
     * @return The nexus player, or null if they're not tracked
     */
    fun get(uniqueId: UUID): NexusPlayer?

    /**
     * Find a [NexusPlayer] by their username
     * @param username The username to look for
     * @return The nexus player, or null if one wasn't found with that username
     */
    fun findByName(username: String?): NexusPlayer?

}