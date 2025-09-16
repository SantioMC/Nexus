package me.santio.nexus.api.entity

import org.bukkit.Location
import java.util.*

/**
 * Holds all virtual entities across the entire cluster
 * @author santio
 */
interface Virtualization {

    /**
     * Get the list of all virtual entities registered with Nexus
     * @return The list of virtual entities
     */
    val virtualEntities: List<VirtualEntity>

    /**
     * Create a managed fake virtual entity that is managed by receiving events from another node
     * @param entity The [VirtualEntity] to create
     * @param location The location to spawn the virtual entity at
     * @param <E> The type of entity being created
     * @param <C> The craft entity type
     * @return The entity that was added
     */
    fun <E : VirtualEntity> createVirtualEntity(entity: E, location: Location): E

    /**
     * Get a virtual entity by its unique id and class, it will only return a value if
     * the entity with this uuid matches the requested entity kind
     * @param uniqueId The unique identifier of the entity, if it's a player then it's the same
     * unique identifier as the player
     * @param clazz The class of the entity kind
     * @param <E> The type of virtual entity
     * @param <C> The craft entity type
     * @return The virtual entity, or null if not found
     */
    fun <E : VirtualEntity> getVirtualEntity(uniqueId: UUID, clazz: Class<E>): E?

    /**
     * Get a virtual entity by its entity id and class, it will only return a value if
     * the entity with this id matches the requested entity kind
     * @param entityId The entity id of the entity
     * @param clazz The class of the entity kind
     * @param <E> The type of virtual entity
     * @param <C> The craft entity type
     * @return The virtual entity, or null if not found
     */
    fun <E : VirtualEntity> getVirtualEntity(entityId: Int, clazz: Class<E>): E?

    /**
     * Delete a virtual entity by its unique id
     * @param uniqueId The unique id of the entity
     */
    fun removeVirtualEntity(uniqueId: UUID)
}
