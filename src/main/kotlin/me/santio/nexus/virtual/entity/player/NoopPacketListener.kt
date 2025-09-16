package me.santio.nexus.virtual.entity.player

import net.minecraft.network.Connection
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.network.ServerGamePacketListenerImpl

/**
 * A no-operation packet listener, which ignores all calls sent towards it
 * @author santio
 */
internal class NoopPacketListener(serverPlayer: ServerPlayer) : ServerGamePacketListenerImpl(
    MinecraftServer.getServer(),
    Connection(PacketFlow.CLIENTBOUND),
    serverPlayer,
    CommonListenerCookie(
        serverPlayer.gameProfile,
        0,
        serverPlayer.clientInformation(),
        false
    )
)
