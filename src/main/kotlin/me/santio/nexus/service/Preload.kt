package me.santio.nexus.service

/**
 * A serviceable interface that allows for items to be preloaded to ensure smooth shutdown
 * @author santio
 */
interface Preload {

    fun preload()

}