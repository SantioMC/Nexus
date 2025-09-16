package me.santio.nexus.api.entity

import me.santio.nexus.api.entity.data.Hand
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.inventory.EntityEquipment
import org.bukkit.util.Vector
import java.util.*

/**
 * A virtual entity is a packet-based entity that exists on another node
 * @author santio
 */
interface VirtualEntity {

    /**
     * The identifier to uniquely identify this entity
     */
    val uniqueId: UUID

    /**
     * The node identifier that this entity is actually located on
     */
    val node: String

    /**
     * The unique numerical id identifying this virtual entity in the server. This
     * entity id is different for each node, you should use [uniqueId] when referencing
     * the same entity between nodes.
     */
    val entityId: Int

    /**
     * The current location of the entity, or null if the entity doesn't have one yet
     */
    val location: Location?

    /**
     * The list of player unique identifiers on the caller node that are being shown this virtual entity
     */
    val viewers: Set<UUID>

    /**
     * Whether this virtual entity is currently on the floor (players can fake this!)
     */
    var onGround: Boolean

    /**
     * Whether the virtual entity is sprinting / running
     */
    var sprinting: Boolean

    /**
     * The pose that the entity is currently doing
     */
    var pose: Pose

    /**
     * Whether the entity is using their hand (drawing back bow, using riptide)
     */
    var usingHand: Hand?

    /**
     * Adds the entity to the player's view. You normally don't want to interact with this as the
     * player ticker will manage entity viewing for you based on your chunks loaded & distance
     *
     * @param viewer The player to spawn the entity to
     * @param data The entity data associated
     * @param velocity The entity starting velocity
     */
    fun spawn(viewer: Player, data: Int = 0, velocity: Vector? = null)

    /**
     * Removes the entity from the players. You normally don't want to interact with this as the
     * player ticker will manage entity viewing for you based on your chunks loaded & distance
     *
     * @param viewer The player to remove this entity from
     */
    fun remove(viewer: Player)

    /**
     * Send an updated metadata packet to all viewers, this is typically done for you
     */
    fun updateMetadata()

    /**
     * Mimics the entity walking in a certain location
     * @param delta The deltas in coordinates of where the entity moved to
     */
    fun walk(delta: Location)

    /**
     * Teleport the entity to a certain location
     * @param location The location to teleport to
     */
    fun teleport(location: Location)

    /**
     * Makes the virtual player swing their hand
     * @param hand The hand to swing, only accepting main or offhand
     * @throws IllegalArgumentException if the provided equipment slot isn't a hand
     */
    fun swingHand(hand: Hand)

    /**
     * Sets the held item for this entity to the specified item stack
     * @param equipment The inventory equipment to set for the player
     */
    fun setEquipment(equipment: EntityEquipment?)

    /**
     * Mock an attack from an outside source damaging this virtual entity
     * @param location The location this entity was attacked from
     * @param attacker The entity id of the entity attacking, or 0
     */
    fun damaged(location: Vector, attacker: Int)

    /**
     * Mimics a realistic attack by temporarily making a new NMS entity and attacking as if it
     * was a real entity on the server
     * @param entity The entity to attack
     */
    fun attack(entity: Entity)

    /**
     * Get nearby players who can get affected by packets from this entity
     * @return A list of nearby players
     */
    fun nearby(): MutableList<out Player>

    companion object {
        /**
         * The maximum range a player can be at before the entity is removed from the client
         */
        const val ENTITY_RANGE: Int = 64

        /**
         * The maximum range we'll even care about for attacking
         */
        const val ATTACK_RANGE: Int = 8

        /**
         * The maximum range Minecraft allows for walking
         */
        const val MAX_WALK_DISTANCE: Double = 7.999755859375
    }
}
