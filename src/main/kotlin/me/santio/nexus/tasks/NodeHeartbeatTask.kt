package me.santio.nexus.tasks

import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.nexus
import me.santio.nexus.data.node.NodeManagerImpl
import me.santio.nexus.packet.node.NodeHeartbeatPacket
import me.santio.nexus.packet.node.NodeRemovePacket
import java.time.Instant

object NodeHeartbeatTask : Runnable {
    override fun run() {
        nexus.cluster().publish(NodeHeartbeatPacket.create(), receive = true)

        // Make sure nodes have been heard from in the last 10 seconds
        for (node in NodeManagerImpl.all()) {
            if (node.id == nexus.id()) continue

            val timeSinceHeartbeat = Instant.now().toEpochMilli() - node.lastResponse
            val timeSinceOnline = Instant.now().toEpochMilli() - node.onlineSince

            if (timeSinceHeartbeat > 10.0e3 && timeSinceOnline > 10.0e3) {
                plugin.logger.info("The node '${node.id}' hasn't been heard in the last 10 seconds, disconnecting the node")
                nexus.cluster().publish(NodeRemovePacket(node.id), receive = true)
                if (node.master) nexus.nodes().electBest()
            }
        }
    }
}
