package me.santio.nexus.data.sign

import com.github.retrooper.packetevents.protocol.chat.ChatType
import com.github.retrooper.packetevents.protocol.chat.ChatTypes
import com.github.retrooper.packetevents.protocol.chat.LastSeenMessages
import com.github.retrooper.packetevents.protocol.chat.MessageSignature
import com.github.retrooper.packetevents.protocol.chat.filter.FilterMask
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_3
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_21_5
import me.santio.nexus.api.player.NexusPlayer
import me.santio.nexus.ext.reflect
import net.kyori.adventure.text.Component
import net.minecraft.network.chat.PlayerChatMessage
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player

internal sealed interface ChatMessageFactory<C : ChatMessage> {

    fun build(player: NexusPlayer, receiver: Player, message: PlayerChatMessage): C

    object v1_21_5: ChatMessageFactory<ChatMessage_v1_21_5> {
        override fun build(player: NexusPlayer, receiver: Player, message: PlayerChatMessage): ChatMessage_v1_21_5 {
            val connection = (receiver as CraftPlayer).handle.connection
            val reflectedClass = connection::class.reflect(connection)
            val nextChatIndex = reflectedClass.get<Int>("nextChatIndex")
                ?: error("Failed to get next chat index")

            reflectedClass.set("nextChatIndex", nextChatIndex + 1)

            return ChatMessage_v1_21_5(
                nextChatIndex + 1,
                message.sender(),
                message.link.index,
                message.signature?.bytes,
                message.signedContent(),
                message.timeStamp(),
                message.salt(),
                LastSeenMessages.Packed(
                    message.signedBody().lastSeen()
                        .entries()
                        .map { signature -> MessageSignature.Packed(MessageSignature(signature.bytes)) }
                ),
                message.adventureView().unsignedContent(),
                FilterMask.PASS_THROUGH,
                ChatType.Bound(
                    ChatTypes.CHAT,
                    Component.text(player.username),
                    null
                )
            )
        }
    }

    object v1_19_3: ChatMessageFactory<ChatMessage_v1_19_3> {
        override fun build(player: NexusPlayer, receiver: Player, message: PlayerChatMessage): ChatMessage_v1_19_3 {
            return ChatMessage_v1_19_3(
                message.sender(),
                message.link.index,
                message.signature?.bytes,
                message.signedContent(),
                message.timeStamp(),
                message.salt(),
                LastSeenMessages.Packed(
                    message.signedBody().lastSeen()
                        .entries()
                        .map { signature -> MessageSignature.Packed(MessageSignature(signature.bytes)) }
                ),
                message.adventureView().unsignedContent(),
                FilterMask.PASS_THROUGH,
                ChatType.Bound(
                    ChatTypes.CHAT,
                    Component.text(player.username),
                    null
                )
            )
        }
    }

}