package me.santio.nexus.data.node

import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.nexus
import me.santio.nexus.api.node.NodeManager
import me.santio.nexus.packet.node.NodeElectedPacket
import me.santio.nexus.packet.node.NodeHeartbeatPacket
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

object NodeManagerImpl: NodeManager {
    private val nodes = ConcurrentHashMap<String, NodeImpl>()

    override fun get(id: String): NodeImpl? {
        return this.nodes.get(id)
    }

    override fun all(): List<NodeImpl> {
        return this.nodes.values.toList()
    }

    /**
     * Add a node to this manager's cache
     * @param node The node to add
     */
    fun add(node: NodeImpl) {
        if (this.nodes.containsKey(node.id)) {
            this.remove(node.id)
        }

        this.nodes.put(node.id, node)
    }

    override fun size(): Int {
        return this.nodes.size
    }

    /**
     * Remove a node from this manager's cache list
     * @param id The node identifier
     */
    fun remove(id: String) {
        this.nodes.remove(id)
    }

    /**
     * Track an incoming heartbeat packet for a node and update its information
     * @param id The node identifier
     * @param data The packet data of the heartbeat
     */
    fun heartbeat(id: String, data: NodeHeartbeatPacket) {
        if (id == plugin.id()) return

        val node = this.get(id) ?: run {
            plugin.logger.severe("Failed to heartbeat unknown node '$id'")
            return
        }

        node.tps = data.tps
        node.mspt = data.mspt
        node.ping = (Instant.now().toEpochMilli() - data.timeSent).toFloat()
        node.maxPlayers = data.maxPlayers
        node.master = data.master
        node.lastResponse = data.timeSent
    }

    override fun elect(node: String) {
        this.master()?.master = false
        val newMaster = nodes.get(node) ?: run {
            plugin.logger.severe("The node '$node' is not known!")
            return
        }

        newMaster.master = true
        nexus.cluster().publish(NodeElectedPacket(newMaster.id))
    }

    override fun electSelf() {
        val self = this.self() ?: run {
            plugin.logger.severe("The server has not yet registered itself in cache as a node, failed to elect self")
            return
        }

        this.elect(self.id)
    }

    override fun electBest(exclusions: List<String>) {
        val bestNode = this.nodes.values
            .stream()
            .filter { node -> !exclusions.contains(node.id) }
            .min(Comparator.comparing(NodeImpl::mspt))
            .orElse(null)

        // No nodes available to elect
        if (bestNode == null) return
        this.elect(bestNode.id)
    }

    override fun master(): NodeImpl? {
        return this.nodes.values
            .stream()
            .filter(NodeImpl::master)
            .findFirst()
            .orElse(null)
    }

    override fun self() = this.get(nexus.id())
}
