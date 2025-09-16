package me.santio.nexus.data.models

import kotlinx.serialization.Serializable
import me.santio.nexus.data.ItemStack
import org.bukkit.inventory.EntityEquipment

@Serializable
data class DisplayInventory(
    val helmet: ItemStack?,
    val chestplate: ItemStack?,
    val leggings: ItemStack?,
    val boots: ItemStack?,
    val mainHand: ItemStack?,
    val offHand: ItemStack?,
) {

    constructor(equipment: EntityEquipment): this(
        equipment.helmet,
        equipment.chestplate,
        equipment.leggings,
        equipment.boots,
        equipment.itemInMainHand,
        equipment.itemInOffHand,
    )

    fun apply(equipment: EntityEquipment) {
        equipment.helmet = helmet
        equipment.chestplate = chestplate
        equipment.leggings = leggings
        equipment.boots = boots
        equipment.setItemInMainHand(mainHand)
        equipment.setItemInOffHand(offHand)
    }

}