package me.santio.nexus.virtual

import me.santio.nexus.api.entity.VirtualEntity
import me.santio.nexus.api.entity.Virtualization
import me.santio.nexus.virtual.entity.PEVirtualEntity
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Holds all virtual entities across the entire cluster
 * @author santio
 */
object PacketEventsVirtualizer: Virtualization {

    private val virtualEntitiesByUniqueId = ConcurrentHashMap<UUID, PEVirtualEntity<*>>()
    private val virtualEntitiesById = ConcurrentHashMap<Int, PEVirtualEntity<*>>()

    override fun <E : VirtualEntity> createVirtualEntity(entity: E, location: Location): E {
        if (entity !is PEVirtualEntity<*>) error("Entity must be a PEVirtualEntity")

        virtualEntitiesByUniqueId.put(entity.uniqueId, entity)
        virtualEntitiesById.put(entity.entityId, entity)
        entity.location = location

        return entity
    }

    override fun <E : VirtualEntity> getVirtualEntity(uniqueId: UUID, clazz: Class<E>): E? {
        val entity = virtualEntitiesByUniqueId.get(uniqueId) ?: return null
        if (!clazz.isInstance(entity)) return null

        return clazz.cast(entity)
    }

    override fun <E : VirtualEntity> getVirtualEntity(entityId: Int, clazz: Class<E>): E? {
        val entity = virtualEntitiesById.get(entityId) ?: return null
        if (!clazz.isInstance(entity)) return null

        return clazz.cast(entity)
    }

    override val virtualEntities: List<PEVirtualEntity<*>>
        get() = virtualEntitiesByUniqueId.values.toList()

    override fun removeVirtualEntity(uniqueId: UUID) {
        val virtualEntity = virtualEntitiesByUniqueId.remove(uniqueId) ?: return

        virtualEntitiesById.remove(virtualEntity.entityId)
        for (player in Bukkit.getOnlinePlayers()) {
            if (!virtualEntity.viewers.contains(player.uniqueId)) continue
            virtualEntity.remove(player)
        }
    }
}
