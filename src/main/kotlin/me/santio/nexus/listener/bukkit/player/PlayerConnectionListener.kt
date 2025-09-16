package me.santio.nexus.listener.bukkit.player

import com.google.auto.service.AutoService
import me.santio.nexus.api.nexus
import me.santio.nexus.packet.player.PlayerConnectPacket
import me.santio.nexus.packet.player.PlayerDisconnectPacket
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@AutoService(Listener::class)
class PlayerConnectionListener: Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        nexus.cluster().publish(PlayerConnectPacket(event.player), receive = true)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        nexus.cluster().publish(PlayerDisconnectPacket(event.player.uniqueId), receive = true)
    }

}