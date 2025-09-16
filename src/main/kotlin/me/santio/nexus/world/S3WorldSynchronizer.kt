package me.santio.nexus.world

import ca.spottedleaf.moonrise.patches.chunk_system.io.MoonriseRegionFileIO
import ca.spottedleaf.moonrise.patches.chunk_system.io.MoonriseRegionFileIO.RegionDataController.ReadData
import com.github.luben.zstd.ZstdOutputStream
import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.api.nexus
import me.santio.nexus.api.world.WorldSynchronizer
import me.santio.nexus.data.CompoundTag
import me.santio.nexus.ext.async
import me.santio.nexus.io.Compression
import me.santio.nexus.packet.world.WorldChunkSyncPacket
import org.bukkit.World
import org.bukkit.craftbukkit.CraftWorld
import java.io.File
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.time.Duration
import java.time.Instant

class S3WorldSynchronizer(private val name: String): WorldSynchronizer {

    private val regionController by lazy { (world as CraftWorld).handle.`moonrise$getChunkDataController`() }

    private val folder: File get() = plugin.server.worldContainer.resolve(name)
        .takeIf { it.resolve("level.dat").exists() }
        ?: error("Failed to get world folder for '$name'!")

    private val world: World get() = plugin.server.getWorld(name)
        ?: error("Attempted to get world '${name}', however it did not exist!")

    private fun getChunk(chunkX: Int, chunkZ: Int): CompoundTag? {
        val chunkData = runCatching {
            regionController.readData(chunkX, chunkZ)
        }.getOrElse {
            plugin.logger.severe("Failed to read chunk data at $chunkX, $chunkZ: $it")
            return null
        }

        if (chunkData.result == ReadData.ReadResult.SYNC_READ) error("A theoretically impossible condition was met")
        if (chunkData.result == ReadData.ReadResult.NO_DATA) return null

        return regionController.finishRead(chunkX, chunkZ, chunkData)
    }

    private fun backup() {
        val backup = folder.parentFile
            .resolve("backup")
            .resolve("backup_${name}_${System.currentTimeMillis()}")

        if (backup.exists()) error("Failed to backup existing non-Nexus world, backup target already exists?")
        if (!backup.parentFile.exists()) backup.parentFile.mkdirs()

        folder.copyRecursively(backup)
    }

    private fun markWorld() {
        folder.resolve(".nexus").takeIf { !it.exists() }?.writeText(DUMMY, charset = Charsets.UTF_8)
    }

    override fun save() {
        val started = Instant.now()

        val output = PipedOutputStream()
        val input = PipedInputStream(output, 4096)

        WorldArchiver.archive(folder, ZstdOutputStream(output))
        plugin.s3.push("world/$name", input)

        val time = Duration.between(started, Instant.now()).toMillis()
        plugin.logger.info("Took ${time}ms to save world '$name' to persistent storage")
    }

    override fun isNexus(): Boolean {
        return folder.resolve(".nexus").exists()
    }

    override fun download() {
        // Check if the world is persisted
        val data = plugin.s3.pull("world/$name") ?: return async {
            // Seems like this is a new world, we'll save the world for other nodes to use
            save()
            markWorld()
        }

        // We need to replace the world, however if the world is not indicated to be a Nexus world, then this is a
        // new node, and we don't want to delete the world, in case it's still needed
        if (!isNexus()) backup()

        // Overwrite partial changes from s3 onto the world
        WorldArchiver.restore(folder, Compression.decompress(data))

        // Mark the world as a Nexus world, so we don't need to back it up next time we download
        markWorld()
    }

    override fun pushChunk(chunkX: Int, chunkZ: Int) {
        val compoundTag = getChunk(chunkX, chunkZ)
            ?: return plugin.logger.warning("Failed to push chunk to other nodes, no chunk data found")

        nexus.cluster().publish(WorldChunkSyncPacket(name, chunkX, chunkZ, compoundTag))
    }

    fun loadChunk(chunkX: Int, chunkZ: Int, compoundTag: CompoundTag) {
        MoonriseRegionFileIO.scheduleSave(
            (world as CraftWorld).handle,
            chunkX,
            chunkZ,
            compoundTag,
            MoonriseRegionFileIO.RegionFileType.CHUNK_DATA
        )
    }

    internal companion object {
        private const val DUMMY = "☃"
    }

}