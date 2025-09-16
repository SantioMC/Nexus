package me.santio.nexus.listener.bukkit.player

import com.google.auto.service.AutoService
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import me.santio.nexus.api.entity.data.Hand.Companion.toHand
import me.santio.nexus.api.nexus
import me.santio.nexus.data.ItemStack
import me.santio.nexus.data.UUID
import me.santio.nexus.packet.entity.EntityUseHandPacket
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot

@AutoService(Listener::class)
class PlayerBowListener: Listener {

    private val trackHolding = setOf(Material.BOW, Material.TRIDENT)
    private val usingItem = mutableSetOf<UUID>()

    private fun startHolding(player: Player, item: ItemStack, hand: EquipmentSlot) {
        val canUseTrident = !player.world.isClearWeather && player.isInRain
        if (item.type == Material.TRIDENT && !canUseTrident) return

        usingItem.add(player.uniqueId)
        nexus.cluster().publish(EntityUseHandPacket(
            uniqueId = player.uniqueId,
            hand = hand.toHand()
        ))
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerDrawBow(event: PlayerInteractEvent) {
        if (event.useItemInHand() == Event.Result.DENY) return
        if (!event.action.isRightClick || event.item?.type !in trackHolding) return
        startHolding(event.player, event.item!!, event.hand!!)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        usingItem.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerStopUsingItem(event: PlayerStopUsingItemEvent) {
        if (event.player.uniqueId !in usingItem) return
        usingItem.remove(event.player.uniqueId)

        nexus.cluster().publish(EntityUseHandPacket(
            uniqueId = event.player.uniqueId,
            hand = null
        ))
    }

}