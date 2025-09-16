package me.santio.nexus.listener.nexus

import com.google.auto.service.AutoService
import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.io.ReceivedPacket
import me.santio.nexus.api.io.subscribe
import me.santio.nexus.api.nexus
import me.santio.nexus.data.node.NodeImpl
import me.santio.nexus.data.node.NodeManagerImpl
import me.santio.nexus.data.player.PlayerManagerImpl
import me.santio.nexus.ext.sync
import me.santio.nexus.packet.node.*
import me.santio.nexus.service.NexusListener

@AutoService(NexusListener::class)
class NodeNexusListener: NexusListener {
    override fun register() {
        nexus.cluster().subscribe<NodeTopographyRequestPacket>(::onTopologyRequest)
        nexus.cluster().subscribe<NodeHeartbeatPacket>(::onHeartbeat)
        nexus.cluster().subscribe<NodeConnectedPacket>(::onNodeConnected)
        nexus.cluster().subscribe<NodeDisconnectedPacket>(::onNodeDisconnected)
        nexus.cluster().subscribe<NodeRemovePacket>(::onNodeRemoved)
        nexus.cluster().subscribe<NodeTopographyPacket>(::onNodeTopology)
        nexus.cluster().subscribe<NodeElectedPacket>(::onNodeElected)
    }

    private fun onTopologyRequest(data: ReceivedPacket<NodeTopographyRequestPacket>) = sync {
        nexus.cluster().publish(NodeTopographyPacket.create(), data.node)
    }

    private fun onHeartbeat(data: ReceivedPacket<NodeHeartbeatPacket>) {
        NodeManagerImpl.heartbeat(data.node, data.event)
    }

    private fun onNodeConnected(data: ReceivedPacket<NodeConnectedPacket>) {
        NodeManagerImpl.add(NodeImpl(data.node, data.event.onlineAt))
        plugin.logger.info("Node ${data.node} connected")
    }

    private fun onNodeRemoved(data: ReceivedPacket<NodeRemovePacket>) {
        NodeManagerImpl.remove(data.event.nodeId)
        plugin.logger.info("Node ${data.event.nodeId} was removed by node ${data.node}")
    }

    private fun onNodeDisconnected(data: ReceivedPacket<NodeDisconnectedPacket>) {
        NodeManagerImpl.remove(data.node)
        plugin.logger.info("Node ${data.node} disconnected")
    }

    private fun onNodeTopology(data: ReceivedPacket<NodeTopographyPacket>) {
        NodeManagerImpl.add(NodeImpl(data.node, data.event.onlineAt))
        NodeManagerImpl.heartbeat(data.node, data.event.heartbeat)

        data.event.players.forEach { event ->
            PlayerManagerImpl.playerConnected(data.node, event)
        }
    }

    private fun onNodeElected(data: ReceivedPacket<NodeElectedPacket>) {
        NodeManagerImpl.master()?.master = false

        val newMaster = nexus.nodes().get(data.event.nodeId)?.let { it as NodeImpl } ?: run {
            plugin.logger.severe(
                "The cluster has elected the new node '${data.event.nodeId}', however it is not known on this node!"
            )

            return
        }

        newMaster.master = true
        plugin.logger.info("The node '${data.event.nodeId}' has been elected as master!")
    }

}