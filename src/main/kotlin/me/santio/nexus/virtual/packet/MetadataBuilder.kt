package me.santio.nexus.virtual.packet

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.item.ItemStack
import com.github.retrooper.packetevents.protocol.player.ClientVersion
import com.github.retrooper.packetevents.protocol.world.painting.PaintingVariant
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.santio.nexus.api.entity.data.Hand
import me.santio.nexus.ext.packetevents
import org.bukkit.entity.Pose

/**
 * A builder to allow for type-safe construction of entity metadata
 * @author santio
 */
@Suppress("unused")
class MetadataBuilder(private val data: MutableList<EntityData<*>> = mutableListOf()) {

    private fun clientVersion(): ClientVersion {
        return packetevents.serverManager.version.toClientVersion()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> modify(index: Int, type: EntityDataType<T>, consumer: (T?) -> T): MetadataBuilder {
        var entityData = this.data
            .firstOrNull { d -> d.index == index }
            ?.let { it as EntityData<T> }

        val current = entityData?.getValue()
        val value = consumer.invoke(current)

        if (entityData != null) {
            entityData.value = value
        } else {
            entityData = EntityData(index, type, value)
            this.data.add(entityData)
        }

        return this
    }

    private fun modifyBitmap(index: Int, bit: Int, add: Boolean): MetadataBuilder {
        return this.modify(index, EntityDataTypes.BYTE) { current: Byte? ->
            val currentData = current ?: 0x0
            if (add) return@modify (currentData.toInt() or bit).toByte()
            else return@modify (currentData.toInt() and bit.inv()).toByte()
        }
    }

    fun onFire(value: Boolean) = this.modifyBitmap(0, 0x01, value)
    fun sneaking(value: Boolean) = this.modifyBitmap(0, 0x02, value)
    fun sprinting(value: Boolean) = this.modifyBitmap(0, 0x08, value)
    fun swimming(value: Boolean) = this.modifyBitmap(0, 0x10, value)
    fun invisible(value: Boolean) = this.modifyBitmap(0, 0x20, value)
    fun glowing(value: Boolean) = this.modifyBitmap(0, 0x40, value)
    fun usingElytra(value: Boolean) = this.modifyBitmap(0, 0x80, value)

    fun pose(pose: Pose) = this.modify(6, EntityDataTypes.ENTITY_POSE) { _ -> SpigotConversionUtil.fromBukkitPose(pose) }
    fun painting(variant: PaintingVariant) = this.modify(8, EntityDataTypes.PAINTING_VARIANT) { _ -> variant }
    fun item(itemStack: ItemStack) = this.modify(8, EntityDataTypes.ITEMSTACK) { _ -> itemStack }
    fun itemRot(rotation: Int) = this.modify(9, EntityDataTypes.INT) { _ -> rotation }
    fun mainHand(right: Boolean) = this.modify(18, EntityDataTypes.BYTE) { _ -> right.compareTo(false).toByte() }
    fun skinLayers(layers: Byte) = this.modify(17, EntityDataTypes.BYTE) { _ -> layers }

    fun usingHand(hand: Hand?, riptide: Boolean = false): MetadataBuilder {
        if (riptide && hand == null) return this.modify(8, EntityDataTypes.BYTE) { _ -> 0b101 }
        if (hand == null) return this.modify(8, EntityDataTypes.BYTE) { _ -> 0b0 }

        this.modifyBitmap(8, 0x01, true)
        this.modifyBitmap(8, 0x02, hand == Hand.OFF_HAND)
        return this.modifyBitmap(8, 0x04, riptide)
    }

    fun build(): List<EntityData<*>> {
        return this.data.toList()
    }

    companion object {
        fun empty(): MetadataBuilder {
            return MetadataBuilder(mutableListOf())
        }

        fun of(entityData: EntityData<*>): MetadataBuilder {
            return MetadataBuilder(mutableListOf(entityData))
        }

        fun of(entityData: MutableList<EntityData<*>>): MetadataBuilder {
            return MetadataBuilder(entityData)
        }
    }
}
