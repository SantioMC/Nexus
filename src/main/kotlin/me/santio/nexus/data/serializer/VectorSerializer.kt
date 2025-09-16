package me.santio.nexus.data.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.util.Vector

object VectorSerializer: KSerializer<Vector> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("me.santio.Vector") {
        element<Float>("x")
        element<Float>("y")
        element<Float>("z")
    }

    override fun serialize(encoder: Encoder, value: Vector) {
        encoder.encodeFloat(value.x.toFloat())
        encoder.encodeFloat(value.y.toFloat())
        encoder.encodeFloat(value.z.toFloat())
    }

    override fun deserialize(decoder: Decoder) = Vector(
        decoder.decodeFloat().toDouble(),
        decoder.decodeFloat().toDouble(),
        decoder.decodeFloat().toDouble(),
    )

}