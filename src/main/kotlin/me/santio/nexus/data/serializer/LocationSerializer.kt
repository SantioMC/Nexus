package me.santio.nexus.data.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.UUID

object LocationSerializer: KSerializer<Location> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("me.santio.Location") {
        element<String>("world")
        element<Float>("x")
        element<Float>("y")
        element<Float>("z")
        element<Float>("yaw")
        element<Float>("pitch")
    }

    override fun serialize(encoder: Encoder, value: Location) {
        encoder.encodeString(value.world?.name ?: "")
        encoder.encodeFloat(value.x.toFloat())
        encoder.encodeFloat(value.y.toFloat())
        encoder.encodeFloat(value.z.toFloat())
        encoder.encodeFloat(value.yaw)
        encoder.encodeFloat(value.pitch)
    }

    override fun deserialize(decoder: Decoder) = Location(
        decoder.decodeString().takeIf { it.isNotEmpty() }?.let { Bukkit.getWorld(it) },
        decoder.decodeFloat().toDouble(),
        decoder.decodeFloat().toDouble(),
        decoder.decodeFloat().toDouble(),
        decoder.decodeFloat(),
        decoder.decodeFloat(),
    )

}