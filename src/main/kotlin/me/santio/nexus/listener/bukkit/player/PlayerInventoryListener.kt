package me.santio.nexus.listener.bukkit.player

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import com.google.auto.service.AutoService
import me.santio.nexus.api.nexus
import me.santio.nexus.data.models.DisplayInventory
import me.santio.nexus.ext.async
import me.santio.nexus.packet.entity.EntityEquipmentPacket
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

@AutoService(Listener::class)
class PlayerInventoryListener: Listener {

    private fun pushInventoryChange(player: Player) = async(delay = 1) {
        nexus.cluster().publish(EntityEquipmentPacket(
            player.uniqueId,
            DisplayInventory(player.equipment)
        ))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerHeldItemChange(event: PlayerItemHeldEvent) {
        pushInventoryChange(event.getPlayer())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onInventoryClick(event: InventoryClickEvent) {
        if (event.slotType !in slotTypes) return
        pushInventoryChange(event.whoClicked as Player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onArmorChange(event: PlayerArmorChangeEvent) {
        pushInventoryChange(event.getPlayer())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onOffhandSwitch(event: PlayerSwapHandItemsEvent) {
        pushInventoryChange(event.getPlayer())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onDrop(event: PlayerDropItemEvent) {
        pushInventoryChange(event.getPlayer())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPickup(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        pushInventoryChange(player)
    }

    private companion object {
        val slotTypes = setOf(
            InventoryType.SlotType.ARMOR,
            InventoryType.SlotType.QUICKBAR
        )
    }

}