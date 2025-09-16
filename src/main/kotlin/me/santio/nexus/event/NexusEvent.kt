package me.santio.nexus.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList


abstract class NexusEvent: Event() {

    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }

    @Suppress("unused") // - Required for Bukkit
    companion object {
        @JvmStatic
        private val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLER_LIST
        }
    }

}