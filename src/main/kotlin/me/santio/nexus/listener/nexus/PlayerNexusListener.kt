package me.santio.nexus.listener.nexus

import com.github.retrooper.packetevents.protocol.chat.RemoteChatSession
import com.google.auto.service.AutoService
import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.entity.VirtualPlayer
import me.santio.nexus.api.getVirtualEntity
import me.santio.nexus.api.io.ReceivedPacket
import me.santio.nexus.api.io.subscribe
import me.santio.nexus.api.nexus
import me.santio.nexus.data.player.NexusPlayerImpl
import me.santio.nexus.data.player.NexusPlayerList
import me.santio.nexus.data.player.PlayerManagerImpl
import me.santio.nexus.data.sign.PlayerChatUtil
import me.santio.nexus.ext.async
import me.santio.nexus.packet.player.*
import me.santio.nexus.service.NexusListener
import net.minecraft.network.chat.PlayerChatMessage
import org.bukkit.Bukkit

@AutoService(NexusListener::class)
class PlayerNexusListener: NexusListener {
    override fun register() {
        nexus.cluster().subscribe<PlayerConnectPacket>(::handleClusterJoin)
        nexus.cluster().subscribe<PlayerDisconnectPacket>(::handleClusterQuit)
        nexus.cluster().subscribe<PlayerSkinLayerPacket>(::handleSkinLayers)
        nexus.cluster().subscribe<PlayerMainHandPacket>(::handleMainHand)
        nexus.cluster().subscribe<PlayerChatSessionPacket>(::handleChatSession)
        nexus.cluster().subscribe<PlayerRiptidePacket>(::handleRiptide)
        nexus.cluster().subscribe<PlayerChatPacket>(::handleChat)
    }

    private fun handleClusterJoin(packet: ReceivedPacket<PlayerConnectPacket>) {
        PlayerManagerImpl.playerConnected(packet.node, packet.event)

        val player = nexus.player(packet.event.uniqueId) ?: return
        val localPlayer = Bukkit.getPlayer(packet.event.uniqueId)

        if (localPlayer == null) {
            NexusPlayerList.addToPlayerList(player as NexusPlayerImpl)
        } else {
            NexusPlayerList.sendPlayerList(localPlayer)
        }
    }

    private fun handleClusterQuit(packet: ReceivedPacket<PlayerDisconnectPacket>) {
        val player = nexus.player(packet.event.uniqueId) ?: return

        val localPlayer = Bukkit.getPlayer(packet.event.uniqueId)
        if (localPlayer != null) return

        NexusPlayerList.removeFromPlayerList(player as NexusPlayerImpl)
        PlayerManagerImpl.remove(packet.event.uniqueId)
    }

    private fun handleSkinLayers(packet: ReceivedPacket<PlayerSkinLayerPacket>) {
        nexus.player(packet.event.uniqueId)?.skinLayers = packet.event.skinLayers
    }

    private fun handleMainHand(packet: ReceivedPacket<PlayerMainHandPacket>) {
        nexus.player(packet.event.uniqueId)?.isRightHanded = packet.event.isRightHand
    }

    private fun handleRiptide(packet: ReceivedPacket<PlayerRiptidePacket>) {
        nexus.virtualization().getVirtualEntity<VirtualPlayer>(packet.event.uniqueId)?.isRiptiding = packet.event.isRiptiding
    }

    private fun handleChatSession(packet: ReceivedPacket<PlayerChatSessionPacket>) = async(delay = 10) {
        val remoteSession = RemoteChatSession(packet.event.sessionId, packet.event.publicProfileKey)
        val player = nexus.player(packet.event.uniqueId)
            ?: return@async plugin.logger.warning("Failed to find NexusPlayer for '${packet.event.uniqueId}', no chat session will be attached")

        (player as NexusPlayerImpl).remoteChatSession = remoteSession
    }

    private fun handleChat(packet: ReceivedPacket<PlayerChatPacket>) {
        val playerChatMessage = plugin.gson.fromJson(packet.event.playerChatMessage, PlayerChatMessage::class.java)

        Bukkit.getOnlinePlayers().forEach {
            PlayerChatUtil.sendChatMessage(it, playerChatMessage)
        }
    }

}