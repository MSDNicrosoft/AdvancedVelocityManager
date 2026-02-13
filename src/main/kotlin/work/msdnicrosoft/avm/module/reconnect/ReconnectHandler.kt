package work.msdnicrosoft.avm.module.reconnect

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction
import com.velocitypowered.proxy.protocol.StateRegistry
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.eventManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.packet.s2c.PlayerAbilitiesPacket
import work.msdnicrosoft.avm.util.component.ComponentSerializer
import work.msdnicrosoft.avm.util.component.orEmpty
import work.msdnicrosoft.avm.util.packet.MinecraftVersion
import work.msdnicrosoft.avm.util.packet.Packet

object ReconnectHandler {
    private inline val config get() = ConfigManager.config.reconnect

    private inline val regex: Regex get() = Regex(config.pattern)

    // https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol_version_numbers
    // https://minecraft.wiki/w/Java_Edition_protocol/Packets#Player_Abilities_(clientbound)
    @Suppress("MagicNumber")
    private val packet: Packet<PlayerAbilitiesPacket> = Packet.of(PlayerAbilitiesPacket::class)
        .direction(Direction.CLIENTBOUND)
        .stateRegistry(StateRegistry.PLAY)
        .packetSupplier(::PlayerAbilitiesPacket)
        .mapping(0x39, MinecraftVersion.MINECRAFT_1_7_2, true)
        .mapping(0x2B, MinecraftVersion.MINECRAFT_1_9, true)
        .mapping(0x2C, MinecraftVersion.MINECRAFT_1_12_1, true)
        .mapping(0x2E, MinecraftVersion.MINECRAFT_1_13, true)
        .mapping(0x31, MinecraftVersion.MINECRAFT_1_14, true)
        .mapping(0x32, MinecraftVersion.MINECRAFT_1_15, true)
        .mapping(0x31, MinecraftVersion.MINECRAFT_1_16, true)
        .mapping(0x30, MinecraftVersion.MINECRAFT_1_16_2, true)
        .mapping(0x32, MinecraftVersion.MINECRAFT_1_17, true)
        .mapping(0x2F, MinecraftVersion.MINECRAFT_1_19, true)
        .mapping(0x31, MinecraftVersion.MINECRAFT_1_19_1, true)
        .mapping(0x30, MinecraftVersion.MINECRAFT_1_19_3, true)
        .mapping(0x34, MinecraftVersion.MINECRAFT_1_19_4, true)
        .mapping(0x36, MinecraftVersion.MINECRAFT_1_20_2, true)
        .mapping(0x38, MinecraftVersion.MINECRAFT_1_20_5, true)
        .mapping(0x3A, MinecraftVersion.MINECRAFT_1_21_2, true)
        .mapping(0x39, MinecraftVersion.MINECRAFT_1_21_5, true)
        .mapping(0x3E, MinecraftVersion.MINECRAFT_1_21_9, true)

    fun init() {
        this.packet.register()
        eventManager.register(plugin, this)
    }

    fun disable() {
        this.packet.unregister()
        eventManager.unregisterListener(plugin, this)
    }

    @Subscribe
    fun onKickedFromServer(event: KickedFromServerEvent): EventTask? {
        val reason: String = ComponentSerializer.BASIC_PLAIN_TEXT.serialize(event.serverKickReason.orEmpty())

        if (!this.regex.matches(reason)) {
            return null
        }

        return EventTask.withContinuation { continuation ->
            Reconnection(event, continuation).reconnect()
        }
    }
}
