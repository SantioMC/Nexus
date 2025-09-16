package me.santio.nexus.api.entity

/**
 * A virtual player is a packet-based player that exists on another node
 * @author santio
 */
interface VirtualPlayer: VirtualEntity {

    /**
     * Whether the player is riptiding
     */
    var isRiptiding: Boolean

}
