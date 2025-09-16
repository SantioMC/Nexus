package me.santio.nexus.data.player

import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.nexus
import me.santio.nexus.api.player.NexusPlayer
import me.santio.nexus.api.player.PlayerManager
import me.santio.nexus.data.node.NodeImpl
import me.santio.nexus.packet.player.PlayerConnectPacket
import me.santio.nexus.virtual.entity.player.PEVirtualPlayer
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Holds information about players on the entire cluster, this includes players from other nodes.
 * @author santio
 */
object PlayerManagerImpl: PlayerManager {
    private val players = ConcurrentHashMap<UUID, NexusPlayerImpl>()

    /**
     * Start tracking a NexusPlayer that is the cluster
     * @param player The player to track
     */
    fun add(player: NexusPlayer) {
        this.players.put(player.uniqueId, player as NexusPlayerImpl)

        val node = nexus.nodes().get(player.node)?.let { it as NodeImpl } ?: run{
            plugin.logger.severe("Attempted to track player ${player.username} on an invalid node")
            return
        }

        node.players.add(player.uniqueId)
    }

    /**
     * Stop tracking a NexusPlayer that is on the cluster
     * @param uniqueId The unique identifier of the player to remove
     */
    fun remove(uniqueId: UUID?) {
        val player = this.players.remove(uniqueId) ?: return
        nexus.virtualization().removeVirtualEntity(player.uniqueId)

        val node = nexus.nodes().get(player.node)?.let { it as NodeImpl } ?: return
        node.players.remove(uniqueId)
    }

    /**
     * Get the list of all NexusPlayers across the entire cluster
     * @return A collection of all players
     */
    override fun all() = this.players.values.toList()

    /**
     * Get the count of players across the entire cluster
     * @return The amount of players online
     */
    override fun size() = this.players.size

    /**
     * Get a cached NexusPlayer from the cluster by their unique identifier
     * @param uniqueId The unique identifier of the player
     * @return The nexus player, or null if they're not tracked
     */
    override fun get(uniqueId: UUID): NexusPlayer? {
        return this.players.get(uniqueId)
    }

    fun playerConnected(node: String, data: PlayerConnectPacket): NexusPlayer {
        val player = NexusPlayerImpl(node, data)
        this.add(player)

        if (Bukkit.getPlayer(player.uniqueId) == null) {
            val virtualPlayer = nexus.virtualization().createVirtualEntity(
                PEVirtualPlayer(player),
                data.location
            )

            virtualPlayer.setEquipment(data.inventory)
        }

        return player
    }

    /**
     * Find a [NexusPlayer] by their username
     * @param username The username to look for
     * @return The nexus player, or null if one wasn't found with that username
     */
    override fun findByName(username: String?): NexusPlayer? {
        return this.players.values
            .stream()
            .filter { player -> player.username.equals(username, ignoreCase = true) }
            .findFirst()
            .orElse(null)
    }
}
