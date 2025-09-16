package me.santio.nexus.listener.nexus

import com.google.auto.service.AutoService
import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.io.ReceivedPacket
import me.santio.nexus.api.io.subscribe
import me.santio.nexus.api.nexus
import me.santio.nexus.packet.world.WorldChunkSyncPacket
import me.santio.nexus.service.NexusListener
import me.santio.nexus.world.S3WorldSynchronizer

@AutoService(NexusListener::class)
class WorldNexusListener: NexusListener {
    override fun register() {
        nexus.cluster().subscribe<WorldChunkSyncPacket>(::handleChunkSync)
    }

    private fun handleChunkSync(packet: ReceivedPacket<WorldChunkSyncPacket>) {
        val world = plugin.server.getWorld(packet.event.world) ?: return
        (nexus.world(world) as S3WorldSynchronizer).loadChunk(
            packet.event.chunkX,
            packet.event.chunkZ,
            packet.event.data,
        )
    }
}