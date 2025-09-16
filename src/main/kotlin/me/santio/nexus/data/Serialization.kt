package me.santio.nexus.data

import com.destroystokyo.paper.profile.ProfileProperty
import kotlinx.serialization.Serializable
import me.santio.nexus.data.serializer.*
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.UUID

typealias UUID = @Serializable(with = UUIDSerializer::class) UUID
typealias Location = @Serializable(with = LocationSerializer::class) Location
typealias Vector = @Serializable(with = VectorSerializer::class) Vector
typealias Component = @Serializable(with = ComponentSerializer::class) Component
typealias ItemStack = @Serializable(with = ItemStackSerializer::class) ItemStack
typealias ProfileProperty = @Serializable(with = ProfilePropertySerializer::class) ProfileProperty
typealias CompoundTag = @Serializable(with = CompoundTagSerializer::class) CompoundTag
