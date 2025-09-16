package me.santio.nexus.virtual.entity.player

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.mojang.authlib.GameProfile
import me.santio.nexus.api.entity.VirtualPlayer
import me.santio.nexus.api.player.NexusPlayer
import me.santio.nexus.virtual.entity.PEVirtualEntity
import me.santio.nexus.virtual.packet.MetadataBuilder
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerPlayer
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import kotlin.math.floor

/**
 * A virtualized player, unlike other virtualized entities, the craft entity is kept persistently and will always
 * return the same instance once created.
 *
 * @author santio
 */
class PEVirtualPlayer(
    val player: NexusPlayer
) : PEVirtualEntity<CraftNexusPlayer>(
    player.node,
    player.uniqueId,
    EntityTypes.PLAYER
), VirtualPlayer {
    private var lastAttacked: Long = 0

    override var isRiptiding: Boolean = false
        set(value) {
            field = value
            this.updateMetadata()
        }

    override fun createMetadata(viewer: Player): MetadataBuilder {
        return MetadataBuilder.empty()
            .mainHand(player.isRightHanded)
            .skinLayers(player.skinLayers)
            .pose(this.pose)
            .sprinting(this.sprinting)
            .sneaking(this.pose == Pose.SNEAKING)
            .usingHand(this.usingHand, riptide = isRiptiding)
    }

    override fun initializeCraftEntity(world: World): CraftNexusPlayer {
        val serverPlayer = ServerPlayer(
            MinecraftServer.getServer(),
            (world as CraftWorld).handle,
            GameProfile(
                player.uniqueId,
                player.username
            ),
            ClientInformation.createDefault()
        )

        serverPlayer.connection = NoopPacketListener(serverPlayer)
        return CraftNexusPlayer(serverPlayer)
    }

    override fun createCraftEntity(world: World?): CraftNexusPlayer? {
        val craftPlayer = super.createCraftEntity(world) ?: return null

        val ticksSinceAttack = floor((System.currentTimeMillis() - this.lastAttacked) / 50.0).toInt()
        craftPlayer.setAttackTicks(ticksSinceAttack)
        this.lastAttacked = System.currentTimeMillis()
        craftPlayer.gameMode = GameMode.CREATIVE

        return craftPlayer
    }
}
