package me.santio.nexus.packet.factory

import me.santio.nexus.packet.entity.EntityNonVirtualizedSpawnPacket
import me.santio.nexus.packet.entity.EntitySpawnPacket
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType

object EntityPacketFactory {

    // pov: backwards compatability
    private val nonVirtualized: Set<(EntityType) -> Boolean> = setOf(
        { it == EntityType.ITEM },
        { it == EntityType.ARROW },
        { it == EntityType.SNOWBALL },
        { it == EntityType.EGG },
        { it == EntityType.EXPERIENCE_ORB },
        { it == EntityType.END_CRYSTAL },
        { it.name == "POTION" || it.name.endsWith("_POTION") },
    )

    fun createSpawnPacket(entity: Entity) = when(nonVirtualized.any { it.invoke(entity.type) }) {
        true -> EntityNonVirtualizedSpawnPacket(entity)
        false -> EntitySpawnPacket(entity)
    }

}