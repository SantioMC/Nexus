package me.santio.nexus.api

interface NexusConfig {

    val isPlayerSync: Boolean
    val isPvPSync: Boolean
    val isMobSync: Boolean
    val isWorldSync: Boolean

}