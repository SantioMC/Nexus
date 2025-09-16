package me.santio.nexus.config

import me.santio.nexus.api.NexusConfig
import org.bukkit.configuration.file.FileConfiguration

class NexusPaperConfig(
    config: FileConfiguration
): NexusConfig {
    override val isPlayerSync: Boolean by lazy { config.getBoolean("features.player-sync", false) }
    override val isPvPSync: Boolean by lazy { config.getBoolean("features.pvp", false) }
    override val isMobSync: Boolean by lazy { config.getBoolean("features.mob-sync", false) }
    override val isWorldSync: Boolean by lazy { config.getBoolean("features.world-sync", false) }
}