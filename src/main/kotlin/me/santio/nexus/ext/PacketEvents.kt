package me.santio.nexus.ext

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.ProtocolPacketEvent
import com.github.retrooper.packetevents.manager.server.ServerVersion
import com.github.retrooper.packetevents.protocol.player.ClientVersion
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import org.bukkit.Bukkit
import org.bukkit.entity.Player

internal val packetevents by lazy {
    PacketEvents.getAPI()
        ?: error("Attempted to access PacketEvents before initialization")
}

internal fun Player.sendPacket(wrapper: PacketWrapper<*>?) {
    if (wrapper == null) return
    packetevents.playerManager.getUser(this)?.sendPacket(wrapper)
}

internal fun Player.sendPacketSilently(wrapper: PacketWrapper<*>?) {
    if (wrapper == null) return
    packetevents.playerManager.getUser(this)?.sendPacketSilently(wrapper)
}

internal fun Iterable<Player>.sendPacket(wrapper: PacketWrapper<*>?) = forEach { it.sendPacket(wrapper) }
internal fun Iterable<Player>.sendPacketSilently(wrapper: PacketWrapper<*>?) = forEach { it.sendPacketSilently(wrapper) }

val ClientVersion.serverVersion: ServerVersion
    get() = ServerVersion.entries.first { it.protocolVersion == this.protocolVersion }

val ProtocolPacketEvent.isFirstPlayer: Boolean
    get() = Bukkit.getOnlinePlayers().minOf { it.entityId } == user.entityId