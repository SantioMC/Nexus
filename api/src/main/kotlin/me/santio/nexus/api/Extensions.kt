package me.santio.nexus.api

/**
 * Kotlin extension methods to assist with working on Nexus
 * @author santio
 */

import me.santio.nexus.api.entity.VirtualEntity
import me.santio.nexus.api.entity.Virtualization
import org.bukkit.Bukkit
import java.util.*

/**
 * Get the Nexus API
 */
val nexus by lazy {
    Bukkit.getServicesManager().load(Nexus::class.java)
        ?: error("Failed to find Nexus instance")
}

/**
 * Helper method for getting virtual entities
 */
inline fun <reified E : VirtualEntity> Virtualization.getVirtualEntity(uniqueId: UUID) =
    this.getVirtualEntity(uniqueId, E::class.java)

/**
 * Helper method for getting virtual entities
 */
inline fun <reified E : VirtualEntity> Virtualization.getVirtualEntity(entityId: Int) =
    this.getVirtualEntity(entityId, E::class.java)