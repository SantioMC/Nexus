package me.santio.nexus.api.player

import com.destroystokyo.paper.profile.ProfileProperty
import me.santio.nexus.api.entity.VirtualPlayer
import net.kyori.adventure.text.Component
import java.util.*

/**
 * Represents a player on the cluster, this player may or may not be on the same node as the
 * caller server.
 *
 * If you are trying to access the data of the actual player entity that players see across
 * nodes, you should interact with [VirtualPlayer] to access data such as location.
 *
 * Changing values here will only apply to the caller node
 *
 * @author santio
 */
interface NexusPlayer {

    /**
     * The identifier of the node that this player is connected to
     */
    val node: String

    /**
     * The unique identifier of the player
     */
    val uniqueId: UUID

    /**
     * The username of the player
     */
    val username: String

    /**
     * The profile data of the player, such as skin
     */
    val properties: Set<ProfileProperty>

    /**
     * Whether the player is currently connected to the caller node
     */
    val isLocal: Boolean

    /**
     * The adventure api component of the tablist name of the player
     */
    val tablistName: Component?

    /**
     * The latency/ping of the player connected to their node
     */
    var latency: Int

    /**
     * The binary representation of skin layers enabled
     */
    var skinLayers: Byte

    /**
     * Whether the player is right-handed
     */
    var isRightHanded: Boolean

    /**
     * Get the virtualized player for this NexusPlayer on this node, if the player is part of the same
     * node, or if the virtualized player isn't ready yet, null is returned instead
     *
     * @return The virtualized player
     */
    fun virtualized(): VirtualPlayer?

}