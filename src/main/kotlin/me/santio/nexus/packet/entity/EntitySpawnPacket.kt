package me.santio.nexus.packet.entity

import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.data.CompoundTag
import me.santio.nexus.data.Location
import me.santio.nexus.data.UUID
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType

@Serializable
data class EntitySpawnPacket(
    val uniqueId: UUID,
    val entityType: EntityType,
    val location: Location,
    val nbt: CompoundTag
) : NexusPacket {
    constructor(entity: Entity): this(
        entity.uniqueId,
        entity.type,
        entity.location,
        (entity as CraftEntity).handle.saveWithoutId(CompoundTag())
    )
}
