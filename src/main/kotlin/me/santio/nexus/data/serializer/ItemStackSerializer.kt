package me.santio.nexus.data.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@OptIn(ExperimentalStdlibApi::class)
object ItemStackSerializer: KSerializer<ItemStack> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("me.santio.ItemStack", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ItemStack) {
        if (value.isEmpty) encoder.encodeString("")
        else encoder.encodeString(value.serializeAsBytes().toHexString())
    }

    override fun deserialize(decoder: Decoder) = decoder.decodeString().takeIf { it.isNotEmpty() }?.let {
        ItemStack.deserializeBytes(it.hexToByteArray())
    } ?: ItemStack(Material.AIR)
}