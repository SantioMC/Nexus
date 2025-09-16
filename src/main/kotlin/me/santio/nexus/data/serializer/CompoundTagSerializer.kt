package me.santio.nexus.data.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import me.santio.nexus.data.buildByteArray
import me.santio.nexus.data.input
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo

@OptIn(ExperimentalStdlibApi::class)
object CompoundTagSerializer: KSerializer<CompoundTag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("me.santio.CompoundTag", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: CompoundTag) {
        val byteArray = buildByteArray { NbtIo.write(value, this) }
        encoder.encodeSerializableValue(serializer<ByteArray>(), byteArray)
    }

    override fun deserialize(decoder: Decoder): CompoundTag {
        val data = decoder.decodeSerializableValue(serializer<ByteArray>())
        if (data.isEmpty()) return CompoundTag()

        return NbtIo.read(data.input())
    }

}