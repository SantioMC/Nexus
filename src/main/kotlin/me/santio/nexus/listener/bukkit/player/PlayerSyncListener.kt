package me.santio.nexus.listener.bukkit.player

import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import com.google.auto.service.AutoService
import io.papermc.paper.event.player.AsyncChatEvent
import io.papermc.paper.event.player.PlayerArmSwingEvent
import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.entity.data.Hand.Companion.toHand
import me.santio.nexus.api.nexus
import me.santio.nexus.ext.async
import me.santio.nexus.ext.diff
import me.santio.nexus.packet.entity.EntityHandAnimationPacket
import me.santio.nexus.packet.entity.EntityMovementPacket
import me.santio.nexus.packet.entity.EntityRemovePacket
import me.santio.nexus.packet.player.PlayerChatPacket
import me.santio.nexus.packet.player.PlayerMainHandPacket
import me.santio.nexus.packet.player.PlayerRiptidePacket
import me.santio.nexus.packet.player.PlayerSkinLayerPacket
import net.minecraft.network.chat.PlayerChatMessage
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPoseChangeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRiptideEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.MainHand

@AutoService(Listener::class)
class PlayerSyncListener: Listener {

    //todo: probably move to a task for reduced context switching
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerMove(event: PlayerMoveEvent) = async {
        nexus.cluster().publish(EntityMovementPacket(
            uniqueId = event.player.uniqueId,
            delta = event.to.diff(event.from)
        ))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerTeleport(event: PlayerTeleportEvent) = async {
        nexus.cluster().publish(EntityMovementPacket(
            uniqueId = event.player.uniqueId,
            delta = event.to.diff(event.from)
        ))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerSettings(event: PlayerClientOptionsChangeEvent) {
        if (event.hasSkinPartsChanged()) {
            nexus.cluster().publish(PlayerSkinLayerPacket(
                uniqueId = event.player.uniqueId,
                skinLayers = event.skinParts.raw.toByte()
            ))
        }

        if (event.hasMainHandChanged()) {
            nexus.cluster().publish(PlayerMainHandPacket(
                uniqueId = event.player.uniqueId,
                isRightHand = event.mainHand == MainHand.RIGHT
            ))
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerSwingHand(event: PlayerArmSwingEvent) {
        nexus.cluster().publish(EntityHandAnimationPacket(
            event.getPlayer().uniqueId,
            event.hand.toHand()
        ))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onXPPickup(event: PlayerPickupExperienceEvent) {
        nexus.cluster().publish(EntityRemovePacket(event.experienceOrb.uniqueId))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerRiptide(event: PlayerRiptideEvent) {
        nexus.cluster().publish(PlayerRiptidePacket(
            uniqueId = event.player.uniqueId,
            isRiptiding = true
        ))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerRiptiding(event: EntityPoseChangeEvent) {
        if (event.entity !is Player) return
        if (event.entity.pose != Pose.SPIN_ATTACK && event.pose != Pose.SPIN_ATTACK) return

        nexus.cluster().publish(PlayerRiptidePacket(
            uniqueId = event.entity.uniqueId,
            isRiptiding = event.pose == Pose.SPIN_ATTACK
        ))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onPlayerChat(event: AsyncChatEvent) {
        val view = event.signedMessage() as PlayerChatMessage.AdventureView
        nexus.cluster().publish(PlayerChatPacket(plugin.gson.toJson(view.playerChatMessage())))
    }

}