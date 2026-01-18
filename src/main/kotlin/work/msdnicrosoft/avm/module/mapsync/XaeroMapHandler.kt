/**
 * Portions of this code are modified from lls-manager
 * https://github.com/plusls/lls-manager/blob/master/src/main/java/com/plusls/llsmanager/minimapWorldSync/PlayerSpawnPosition.java
 */

package work.msdnicrosoft.avm.module.mapsync

import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction
import com.velocitypowered.proxy.protocol.StateRegistry
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.channelRegistrar
import work.msdnicrosoft.avm.packet.SetDefaultSpawnPositionPacket
import work.msdnicrosoft.avm.util.packet.MinecraftVersion
import work.msdnicrosoft.avm.util.packet.Packet

object XaeroMapHandler {
    val XAERO_MINI_MAP_CHANNEL: MinecraftChannelIdentifier =
        MinecraftChannelIdentifier.create("xaerominimap", "main")

    val XAERO_WORLD_MAP_CHANNEL: MinecraftChannelIdentifier =
        MinecraftChannelIdentifier.create("xaeroworldmap", "main")

    // https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol_version_numbers
    // https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Default_Spawn_Position
    @Suppress("MagicNumber")
    private val packet: Packet<SetDefaultSpawnPositionPacket> = Packet.of(SetDefaultSpawnPositionPacket::class)
        .direction(Direction.CLIENTBOUND)
        .stateRegistry(StateRegistry.PLAY)
        .packetSupplier(::SetDefaultSpawnPositionPacket)
        .mapping(0x05, MinecraftVersion.MINECRAFT_1_7_2, false)
        .mapping(0x43, MinecraftVersion.MINECRAFT_1_9, false)
        .mapping(0x45, MinecraftVersion.MINECRAFT_1_12, false)
        .mapping(0x46, MinecraftVersion.MINECRAFT_1_12_1, false)
        .mapping(0x49, MinecraftVersion.MINECRAFT_1_13, false)
        .mapping(0x4D, MinecraftVersion.MINECRAFT_1_14, false)
        .mapping(0x4E, MinecraftVersion.MINECRAFT_1_15, false)
        .mapping(0x42, MinecraftVersion.MINECRAFT_1_16, false)
        .mapping(0x4B, MinecraftVersion.MINECRAFT_1_17, false)
        .mapping(0x4A, MinecraftVersion.MINECRAFT_1_19, false)
        .mapping(0x4D, MinecraftVersion.MINECRAFT_1_19_1, false)
        .mapping(0x4C, MinecraftVersion.MINECRAFT_1_19_3, false)
        .mapping(0x50, MinecraftVersion.MINECRAFT_1_19_4, false)
        .mapping(0x52, MinecraftVersion.MINECRAFT_1_20_2, false)
        .mapping(0x54, MinecraftVersion.MINECRAFT_1_20_3, false)
        .mapping(0x56, MinecraftVersion.MINECRAFT_1_20_5, false)
        .mapping(0x5B, MinecraftVersion.MINECRAFT_1_21_4, false)
        .mapping(0x5A, MinecraftVersion.MINECRAFT_1_21_5, false)
        .mapping(0x5F, MinecraftVersion.MINECRAFT_1_21_11, false)

    fun init() {
        this.packet.register()
        channelRegistrar.register(this.XAERO_WORLD_MAP_CHANNEL, this.XAERO_MINI_MAP_CHANNEL)
    }

    fun disable() {
        this.packet.unregister()
        channelRegistrar.unregister(this.XAERO_WORLD_MAP_CHANNEL, this.XAERO_MINI_MAP_CHANNEL)
    }
}
