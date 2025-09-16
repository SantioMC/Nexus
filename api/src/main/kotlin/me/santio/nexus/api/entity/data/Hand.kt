package me.santio.nexus.api.entity.data

import org.bukkit.inventory.EquipmentSlot

enum class Hand {

    MAIN_HAND,
    OFF_HAND,
    ;

    companion object {
        fun EquipmentSlot.toHand() = when(this) {
            EquipmentSlot.HAND -> MAIN_HAND
            EquipmentSlot.OFF_HAND -> OFF_HAND
            else -> error("EquipmentSlot couldn't be converted to a hand!")
        }
    }

}