package me.santio.nexus.packet.player

import com.destroystokyo.paper.ClientOption
import kotlinx.serialization.Serializable
import me.santio.nexus.api.NexusPacket
import me.santio.nexus.data.Component
import me.santio.nexus.data.Location
import me.santio.nexus.data.ProfileProperty
import me.santio.nexus.data.UUID
import me.santio.nexus.data.models.DisplayInventory
import org.bukkit.entity.Player
import org.bukkit.inventory.MainHand

@Serializable
data class PlayerConnectPacket(
    val uniqueId: UUID,
    val username: String,
    val properties: Set<ProfileProperty>,
    val location: Location,
    val latency: Int,
    val tablistName: Component,
    val inventory: DisplayInventory,
    val skinLayers: Byte,
    val rightHanded: Boolean,
): NexusPacket {

    constructor(player: Player): this(
        player.uniqueId,
        player.name,
        player.playerProfile.properties,
        player.location,
        player.ping,
        player.playerListName(),
        DisplayInventory(player.equipment),
        player.getClientOption(ClientOption.SKIN_PARTS).raw.toByte(),
        player.getClientOption(ClientOption.MAIN_HAND) == MainHand.RIGHT,
    )

}