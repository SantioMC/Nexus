package me.santio.nexus.virtual.entity.player

import me.santio.nexus.NexusPlugin.Companion.plugin
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftPlayer
import java.lang.reflect.Field

/**
 * An extension on top of [CraftPlayer] which implements methods from CraftPlayer to better support
 * cross-server functionality
 *
 * @author santio
 */
class CraftNexusPlayer internal constructor(private val serverPlayer: ServerPlayer)
    : CraftPlayer(plugin.server as CraftServer, serverPlayer) {

    fun setAttackTicks(ticks: Int) {
        try {
            attackStrengthTickerField.set(this.handle, ticks)
        } catch (e: IllegalAccessException) {
            plugin.logger.severe("Failed to set attack strength ticker: $e")
        }
    }

    private companion object {
        val attackStrengthTickerField: Field

        init {
            try {
                attackStrengthTickerField = LivingEntity::class.java.getDeclaredField("attackStrengthTicker")
                attackStrengthTickerField.setAccessible(true)
            } catch (e: NoSuchFieldException) {
                throw ExceptionInInitializerError(e)
            }
        }
    }
}
