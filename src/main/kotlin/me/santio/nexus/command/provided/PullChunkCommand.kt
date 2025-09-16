package me.santio.nexus.command.provided

import com.google.auto.service.AutoService
import me.santio.nexus.command.NexusCommand

@AutoService(NexusCommand::class)
class PullChunkCommand: NexusCommand {

//    @Command("pullchunk")
//    private fun pullChunk(sender: PlayerSource) {
//        val world = nexus.world(sender.source().world)
//        val chunk = sender.source().location.chunk
//
//        sender.source().sendMessage(!"<body>Pulling chunk ${chunk.x}, ${chunk.z}")
//        world.pullChunk(chunk.x, chunk.z)
//        sender.source().sendMessage(!"<success>Pulled chunk")
//    }

}