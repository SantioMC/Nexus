package me.santio.nexus.data.serializer

import com.destroystokyo.paper.profile.ProfileProperty
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ProfilePropertySerializer: KSerializer<ProfileProperty> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("me.santio.ProfileProperty") {
        element<String>("name")
        element<String>("value")
        element<String>("signature")
    }

    override fun serialize(encoder: Encoder, value: ProfileProperty) {
        encoder.encodeString(value.name)
        encoder.encodeString(value.value)
        encoder.encodeString(value.signature ?: "")
    }

    override fun deserialize(decoder: Decoder) = ProfileProperty(
        decoder.decodeString(),
        decoder.decodeString(),
        decoder.decodeString().takeIf { it.isNotEmpty() },
    )
}