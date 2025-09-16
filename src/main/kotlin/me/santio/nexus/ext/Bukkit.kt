package me.santio.nexus.ext

import ca.spottedleaf.moonrise.common.PlatformHooks
import ca.spottedleaf.moonrise.paper.PaperHooks
import me.santio.nexus.NexusPlugin.Companion.plugin
import me.santio.nexus.data.Location
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.World
import org.bukkit.craftbukkit.CraftWorld
import java.util.concurrent.TimeUnit

internal fun sync(delay: Long = 0, block: () -> Unit) {
    if (plugin.server.isPrimaryThread && delay == 0L) block()
    else plugin.server.scheduler.runTaskLater(plugin, block, delay)
}

internal fun async(delay: Long = 0, block: () -> Unit) {
    if (delay == 0L) plugin.executor.execute(block)
    else plugin.scheduler.schedule(block, delay * 50L, TimeUnit.MILLISECONDS)
}

internal fun Location.diff(other: Location) = Location(
    this.world ?: other.world,
    this.x - other.x,
    this.y - other.y,
    this.z - other.z,
    this.yaw - other.yaw,
    this.pitch - other.pitch,
)

internal val paperHooks
    get() = PlatformHooks.get() as PaperHooks

fun World.silentSave(flush: Boolean) {
    val world = (this as CraftWorld).handle
    val oldSave = world.noSave

    world.noSave = false
    world.save(null, flush, false)
    world.noSave = oldSave
}

internal operator fun String.not() = plugin.miniMessage.deserialize(this)
internal fun String.resolve(vararg resolvers: TagResolver) = plugin.miniMessage.deserialize(this, *resolvers)