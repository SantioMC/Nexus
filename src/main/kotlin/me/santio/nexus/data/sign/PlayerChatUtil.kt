package me.santio.nexus.data.sign

import com.github.retrooper.packetevents.manager.server.ServerVersion
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage
import io.papermc.paper.adventure.PaperAdventure
import me.santio.nexus.api.nexus
import me.santio.nexus.ext.packetevents
import me.santio.nexus.ext.reflect
import me.santio.nexus.ext.sendPacketSilently
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.network.chat.LastSeenMessagesValidator
import net.minecraft.network.chat.MessageSignatureCache
import net.minecraft.network.chat.PlayerChatMessage
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerKickEvent

object PlayerChatUtil {

    fun buildChatMessage(viewer: Player, message: PlayerChatMessage): ChatMessage? {
        val serverVersion = packetevents.serverManager.version
        val player = nexus.player(message.sender()) ?: return null

        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
            return ChatMessageFactory.v1_21_5.build(player, viewer, message)
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            return ChatMessageFactory.v1_19_3.build(player, viewer, message)
        }

        error("This version of Minecraft is not supported on Nexus, please update to at least v1.19.3")
    }

    fun sendChatMessage(viewer: Player, message: PlayerChatMessage) {
        val connection = (viewer as CraftPlayer).handle.connection
        val reflected = connection::class.reflect(connection)

        val messageSignatureCache = reflected.get<MessageSignatureCache>("messageSignatureCache")
            ?: error("Failed to get message signature cache, please update Nexus")

        val lastSeenMessages = reflected.get<LastSeenMessagesValidator>("lastSeenMessages")
            ?: error("Failed to get last seen messages validator, please update Nexus")

        viewer.sendPacketSilently(WrapperPlayServerChatMessage(buildChatMessage(viewer, message)))

        message.signature?.let { signature ->
            messageSignatureCache.push(message.signedBody, signature)
            lastSeenMessages.addPending(signature)

            val pendingChats = lastSeenMessages.trackedMessagesCount()
            if (pendingChats > 4096) connection.disconnectAsync(PaperAdventure.asVanilla(
                Component.text("(Nexus) You have too many pending chats!", NamedTextColor.RED)
            ), PlayerKickEvent.Cause.TOO_MANY_PENDING_CHATS)
        }
    }

}