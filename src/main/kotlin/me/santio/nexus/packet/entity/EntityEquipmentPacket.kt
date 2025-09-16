package me.santio.nexus.packet.entity

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.data.UUID
import me.santio.nexus.data.models.DisplayInventory

@Serializable
data class EntityEquipmentPacket(
    val uniqueId: UUID,
    val inventory: DisplayInventory,
) : NexusPacket
