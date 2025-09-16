package me.santio.nexus.api.node

import java.util.*

interface Node {
    /**
     * The unique identifier of this node
     */
    val id: String

    /**
     * The timestamp of when this node came online
     */
    val onlineSince: Long

    /**
     * The amount of ticks per second the node is running at
     */
    val tps: Float

    /**
     * The average milliseconds for each tick
     */
    val mspt: Float

    /**
     * The time it took for this node to receive the last heartbeat
     */
    val ping: Float

    /**
     * The maximum amount of players allowed on the node
     */
    val maxPlayers: Int

    /**
     * Whether this node is in charge of handling primary tasks such as the world, time, weather, etc.
     */
    val master: Boolean

    /**
     * The last timestamp the last heartbeat by this node was heard
     */
    val lastResponse: Long

    /**
     * The amount of players on the node
     */
    val players: Set<UUID>

    /**
     * The unique identifiers of entities that exist on this node
     */
    val entities: Set<UUID>
}