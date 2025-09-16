package me.santio.nexus.data.node

import me.santio.nexus.api.node.Node
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class NodeImpl(
    override val id: String,
    override val onlineSince: Long = 0,
    override var tps: Float = 0f,
    override var mspt: Float = 0f,
    override var ping: Float = 0f,
    override var maxPlayers: Int = 0,
    override var master: Boolean = false,
    override var lastResponse: Long = 0,
    override val players: MutableSet<UUID> = ConcurrentHashMap.newKeySet(),
    override val entities: MutableSet<UUID> = ConcurrentHashMap.newKeySet(),
): Node
