package me.santio.nexus.listener.packet

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import com.google.auto.service.AutoService
import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.entity.VirtualEntity
import me.santio.nexus.api.getVirtualEntity
import me.santio.nexus.api.nexus
import me.santio.nexus.packet.entity.AttackEntityPacket
import org.bukkit.entity.Player

@AutoService(PacketListener::class)
class PVPPacketListener : PacketListener {
    override fun onPacketReceive(event: PacketReceiveEvent) {
        if (event.packetType != PacketType.Play.Client.INTERACT_ENTITY || event.user == null) return
        val player = event.getPlayer<Player>() ?: return

        val packet = WrapperPlayClientInteractEntity(event)
        if (packet.action != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return

        val entity = nexus.virtualization().getVirtualEntity<VirtualEntity>(packet.entityId)
            ?: return plugin.logger.warning("${player.name} attempted to attack untracked entity: ${packet.entityId}")

        nexus.cluster().publish(AttackEntityPacket(
            entity.uniqueId,
            player.uniqueId
        ), target = entity.node)
    }
}
