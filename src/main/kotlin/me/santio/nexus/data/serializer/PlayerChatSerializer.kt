package me.santio.nexus.data.serializer

import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.santio.nexus.NexusPlugin.Companion.plugin
import net.minecraft.network.chat.PlayerChatMessage

object PlayerChatSerializer: KSerializer<PlayerChatMessage> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("me.santio.PlayerChatMessage", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PlayerChatMessage) {
        val result = PlayerChatMessage.MAP_CODEC.encoder().encodeStart(JsonOps.INSTANCE, value)
            ?: error("Failed to encode PlayerChatMessage")

        encoder.encodeString(result.orThrow.toString())
    }

    override fun deserialize(decoder: Decoder): PlayerChatMessage {
        val jsonElement = PlayerChatMessage.MAP_CODEC.decoder()
            .decode(JsonOps.INSTANCE, plugin.gson.fromJson(decoder.decodeString(), JsonElement::class.java))
            ?: error("Failed to decode PlayerChatMessage")

        return jsonElement.orThrow.first
    }

}