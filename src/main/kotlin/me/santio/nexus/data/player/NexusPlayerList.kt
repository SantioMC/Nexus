package me.santio.nexus.data.player

import me.santio.nexus.api.nexus
import me.santio.nexus.ext.sendPacket
import me.santio.nexus.virtual.packet.VirtualPlayerPacket
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object NexusPlayerList {

    /**
     * Sends the network player list to the player, this will add all players from
     * other nodes to this player's tablist
     * @param player The player to send the list to
     */
    fun sendPlayerList(player: Player) {
        for (other in nexus.players().all()) {
            if (other.uniqueId === player.uniqueId) continue
            if (other.node == nexus.id()) continue

            player.sendPacket(VirtualPlayerPacket.createPlayerInfoPacket(
                VirtualPlayerPacket.TablistChange.from(other),
                false
            ))
        }
    }

    fun addToPlayerList(newPlayer: NexusPlayerImpl) {
        for (player in Bukkit.getOnlinePlayers()) {
            player.sendPacket(VirtualPlayerPacket.createPlayerInfoPacket(
                VirtualPlayerPacket.TablistChange.from(newPlayer),
                remove = false
            ))
        }
    }

    fun removeFromPlayerList(nexusPlayer: NexusPlayerImpl) {
        for (player in Bukkit.getOnlinePlayers()) {
            player.sendPacket(VirtualPlayerPacket.createPlayerInfoPacket(
                VirtualPlayerPacket.TablistChange.from(nexusPlayer),
                remove = true
            ))
        }
    }

    fun updatePlayerTablist(change: VirtualPlayerPacket.TablistChange) {
        for (other in Bukkit.getOnlinePlayers()) {
            other.sendPacket(VirtualPlayerPacket.updatePlayerInfoPacket(change))
        }
    }
}
