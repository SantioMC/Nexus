package me.santio.nexus.data.serializer

import com.github.retrooper.packetevents.protocol.player.PublicProfileKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import net.minecraft.util.Crypt
import java.security.PublicKey
import java.time.Instant

object PublicProfileSerializer: KSerializer<PublicProfileKey> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("me.santio.PublicProfileKey") {
        element<Long>("expiresAt")
        element<PublicKey>("key")
        element<ByteArray>("keySignature")
    }

    override fun serialize(encoder: Encoder, value: PublicProfileKey) {
        encoder.encodeLong(value.expiresAt.toEpochMilli())
        encoder.encodeSerializableValue(serializer<ByteArray>(), value.key.encoded)
        encoder.encodeSerializableValue(serializer<ByteArray>(), value.keySignature)
    }

    override fun deserialize(decoder: Decoder) = PublicProfileKey(
        Instant.ofEpochMilli(decoder.decodeLong()),
        Crypt.byteToPublicKey(decoder.decodeSerializableValue(serializer<ByteArray>())),
        decoder.decodeSerializableValue(serializer<ByteArray>()),
    )

}