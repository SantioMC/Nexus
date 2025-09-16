package me.santio.nexus.data.player

import com.destroystokyo.paper.profile.ProfileProperty
import com.github.retrooper.packetevents.protocol.chat.RemoteChatSession
import com.github.retrooper.packetevents.protocol.player.TextureProperty
import com.github.retrooper.packetevents.protocol.player.UserProfile
import me.santio.nexus.api.getVirtualEntity
import me.santio.nexus.api.nexus
import me.santio.nexus.api.player.NexusPlayer
import me.santio.nexus.packet.player.PlayerConnectPacket
import me.santio.nexus.virtual.PacketEventsVirtualizer
import me.santio.nexus.virtual.entity.player.PEVirtualPlayer
import me.santio.nexus.virtual.packet.VirtualPlayerPacket
import net.kyori.adventure.text.Component
import java.util.*

/**
 * A representation of a player, that could be on any node in the cluster
 * @author santio
 */
@Suppress("LocalVariableName")
class NexusPlayerImpl internal constructor(
    override val node: String,
    override val uniqueId: UUID,
    override val username: String,
    _skinLayers: Byte,
    _rightHanded: Boolean,
    _properties: Set<ProfileProperty>,
    _latency: Int,
    _tablistName: Component?,
): NexusPlayer {

    override var properties: Set<ProfileProperty> = _properties; private set
    override var tablistName: Component? = _tablistName; private set

    var remoteChatSession: RemoteChatSession? = null
        set(value) {
            field = value
            updateTablist()
        }

    override var skinLayers: Byte = _skinLayers
        set(value) {
            field = value
            virtualized()?.updateMetadata()
        }

    override var isRightHanded: Boolean = _rightHanded
        set(value) {
            field = value
            virtualized()?.updateMetadata()
        }

    override var latency: Int = _latency
        set(value) {
            field = value
            updateTablist()
        }

    internal constructor(node: String, packet: PlayerConnectPacket): this(
        node, packet.uniqueId, packet.username, packet.skinLayers,
        packet.rightHanded, packet.properties, packet.latency, packet.tablistName
    )

    override val isLocal = node == nexus.id()

    /**
     * Create a PacketEvents [UserProfile] for this player
     * @return A user profile for the protocol layer
     */
    fun createProfile(): UserProfile {
        return UserProfile(
            uniqueId,
            username,
            properties.map { data ->
                TextureProperty(data.name, data.value, data.signature)
            }
        )
    }

    override fun virtualized() = PacketEventsVirtualizer.getVirtualEntity<PEVirtualPlayer>(uniqueId)

    /**
     * Update the tablist with the latest data
     */
    private fun updateTablist() {
        NexusPlayerList.updatePlayerTablist(VirtualPlayerPacket.TablistChange.from(this))
    }
}
