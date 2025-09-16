package me.santio.nexus.api.world

import org.jetbrains.annotations.Blocking

/**
 * The world synchronizer is responsible for handling synchronization between nodes for a specified world. Working with
 * this class is considered dangerous and data loss can occur if you are unaware of what you are doing. Caution is
 * recommended when working with this class.
 *
 * @author santio
 */
interface WorldSynchronizer {

    /**
     * Replaces the world files with the ones from remote storage. **This is a destructive action** and will cause
     * data loss if called anytime while the server is running.
     */
    @Blocking
    fun download()

    /**
     * Saves the current world files to the remote storage, you should ensure that this is called after a world save
     * so that the files are written to disk as the data in memory isn't used.
     */
    @Blocking
    fun save()

    /**
     * Checks if the world has a marker indicating it was pulled in from persistent storage, this will indicate that
     * (parts of) the world can be safely modified and deleted by Nexus without taking backups.
     */
    fun isNexus(): Boolean

    /**
     * Pushes the chunk data to the other nodes to import into their region files, this should only be done for
     * chunks that are newly generated. Pushing a chunk that another server has loaded already will likely cause
     * the data to be replaced.
     *
     * @param chunkX The x-coordinate of the chunk
     * @param chunkZ The z-coordinate of the chunk
     */
    fun pushChunk(chunkX: Int, chunkZ: Int)

}