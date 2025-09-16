package me.santio.nexus.virtual.packet

import com.github.retrooper.packetevents.manager.server.ServerVersion
import com.github.retrooper.packetevents.protocol.chat.RemoteChatSession
import com.github.retrooper.packetevents.protocol.player.GameMode
import com.github.retrooper.packetevents.protocol.player.UserProfile
import com.github.retrooper.packetevents.util.crypto.SignatureData
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.santio.nexus.api.player.NexusPlayer
import me.santio.nexus.data.player.NexusPlayerImpl
import me.santio.nexus.ext.packetevents
import net.kyori.adventure.text.Component
import java.util.*

/**
 * Utility class for making packets for virtual players
 * @author santio
 */
object VirtualPlayerPacket {
    fun createPlayerInfoPacket(
        data: TablistChange,
        remove: Boolean
    ): PacketWrapper<*> {
        val version = packetevents.serverManager.version

        if (version.isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            if (remove) {
                return WrapperPlayServerPlayerInfoRemove(mutableListOf(data.userProfile.uuid))
            }

            val actions: EnumSet<WrapperPlayServerPlayerInfoUpdate.Action?> = data.actions.clone()
            actions.add(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER)

            return WrapperPlayServerPlayerInfoUpdate(
                actions,
                mutableListOf(data.toPlayerInfoUpdate())
            )
        } else {
            return WrapperPlayServerPlayerInfo(
                if (remove) WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER else WrapperPlayServerPlayerInfo.Action.ADD_PLAYER,
                data.toPlayerData()
            )
        }
    }

    fun updatePlayerInfoPacket(change: TablistChange): PacketWrapper<*> {
        val version = packetevents.serverManager.version

        return if (version.isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
            WrapperPlayServerPlayerInfoUpdate(
                change.actions,
                mutableListOf(change.toPlayerInfoUpdate())
            )
        } else {
            WrapperPlayServerPlayerInfo(
                WrapperPlayServerPlayerInfo.Action.UPDATE_LATENCY,
                change.toPlayerData()
            )
        }
    }

    class TablistChange(
        val userProfile: UserProfile
    ) {
        val actions: EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> =
            EnumSet.noneOf(WrapperPlayServerPlayerInfoUpdate.Action::class.java)

        private var displayName: Component? = null
        private var gamemode = GameMode.CREATIVE
        private var latency = 0
        private var priority = 0
        private var listed = true
        private var showHat = true
        private var signatureData: RemoteChatSession? = null

        /*
        @Nullable Component displayName, @Nullable UserProfile userProfile, @Nullable GameMode gameMode, int ping
         */
        fun displayName(component: Component?): TablistChange {
            this.actions.add(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME)
            this.displayName = component
            return this
        }

        fun gamemode(gamemode: org.bukkit.GameMode): TablistChange {
            this.actions.add(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE)
            this.gamemode = SpigotConversionUtil.fromBukkitGameMode(gamemode)
            return this
        }

        fun latency(latency: Int): TablistChange {
            this.actions.add(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY)
            this.latency = latency
            return this
        }

        fun signature(signature: RemoteChatSession): TablistChange {
            this.actions.add(WrapperPlayServerPlayerInfoUpdate.Action.INITIALIZE_CHAT)
            this.signatureData = signature
            return this
        }

        fun priority(priority: Int): TablistChange {
            this.actions.add(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LIST_ORDER)
            this.priority = priority
            return this
        }

        fun listed(listed: Boolean): TablistChange {
            this.actions.add(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED)
            this.listed = listed
            return this
        }

        fun showHat(shown: Boolean): TablistChange {
            this.actions.add(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_HAT)
            this.showHat = shown
            return this
        }

        /**
         * Create player data for the pre-1.19.3 tablist packet
         * @return The [WrapperPlayServerPlayerInfo.PlayerData]
         */
        fun toPlayerData(): WrapperPlayServerPlayerInfo.PlayerData {
            return WrapperPlayServerPlayerInfo.PlayerData(
                this.displayName,
                this.userProfile,
                this.gamemode,
                this.signatureData?.let { SignatureData(
                    it.publicProfileKey.expiresAt,
                    it.publicProfileKey.key,
                    it.publicProfileKey.keySignature
                ) },
                this.latency
            )
        }

        fun toPlayerInfoUpdate(): WrapperPlayServerPlayerInfoUpdate.PlayerInfo {
            return WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                this.userProfile,
                this.listed,
                this.latency,
                this.gamemode,
                this.displayName,
                this.signatureData,
                this.priority,
                this.showHat
            )
        }

        companion object {
            fun from(player: NexusPlayer): TablistChange {
                val player = player as NexusPlayerImpl
                val change = TablistChange(player.createProfile())

                change.showHat(true)
                change.listed(true)
                change.latency(player.latency)

                player.remoteChatSession?.let { change.signature(it) }
                player.tablistName?.let { change.displayName(it) }

                //            todo: change.gamemode()

                return change
            }

            fun empty(player: NexusPlayer): TablistChange {
                val player = player as NexusPlayerImpl
                return TablistChange(player.createProfile())
            }
        }
    }
}
