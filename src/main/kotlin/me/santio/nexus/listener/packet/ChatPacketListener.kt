package me.santio.nexus.listener.packet

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatSessionUpdate
import com.google.auto.service.AutoService
import me.santio.nexus.api.nexus
import me.santio.nexus.packet.player.PlayerChatSessionPacket

@AutoService(PacketListener::class)
class ChatPacketListener: PacketListener {

    override fun onPacketReceive(event: PacketReceiveEvent) {
        if (event.packetType != PacketType.Play.Client.CHAT_SESSION_UPDATE) return
        if (event.user == null) return

        val wrapper = WrapperPlayClientChatSessionUpdate(event)
        nexus.cluster().publish(PlayerChatSessionPacket(
            event.user.uuid,
            wrapper.chatSession.sessionId,
            wrapper.chatSession.publicProfileKey
        ))
    }

}