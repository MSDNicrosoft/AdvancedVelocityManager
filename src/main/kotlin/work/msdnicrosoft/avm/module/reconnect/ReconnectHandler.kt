package work.msdnicrosoft.avm.module.reconnect

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction
import com.velocitypowered.proxy.protocol.StateRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.flattener.ComponentFlattener
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.packet.s2c.PlayerAbilitiesPacket
import work.msdnicrosoft.avm.util.packet.Packet
import work.msdnicrosoft.avm.util.packet.Packet.Companion.mapping

object ReconnectHandler {

    private val serializer = PlainTextComponentSerializer.builder()
        .flattener(ComponentFlattener.basic())
        .build()

    private val regex =
        Regex("((?i)^(server closed|server is restarting|multiplayer\\.disconnect\\.server_shutdown))+$")

    // https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol_version_numbers
    // https://minecraft.wiki/w/Java_Edition_protocol/Packets#Player_Abilities_(clientbound)
    @Suppress("MagicNumber")
    private val MAPPINGS = listOf(
        mapping(0x39, ProtocolVersion.MINECRAFT_1_7_2, true),
        mapping(0x2B, ProtocolVersion.MINECRAFT_1_9, true),
        mapping(0x2C, ProtocolVersion.MINECRAFT_1_12_1, true),
        mapping(0x2E, ProtocolVersion.MINECRAFT_1_13, true),
        mapping(0x31, ProtocolVersion.MINECRAFT_1_14, true),
        mapping(0x32, ProtocolVersion.MINECRAFT_1_15, true),
        mapping(0x31, ProtocolVersion.MINECRAFT_1_16, true),
        mapping(0x30, ProtocolVersion.MINECRAFT_1_16_2, true),
        mapping(0x32, ProtocolVersion.MINECRAFT_1_17, true),
        mapping(0x2F, ProtocolVersion.MINECRAFT_1_19, true),
        mapping(0x31, ProtocolVersion.MINECRAFT_1_19_1, true),
        mapping(0x30, ProtocolVersion.MINECRAFT_1_19_3, true),
        mapping(0x34, ProtocolVersion.MINECRAFT_1_19_4, true),
        mapping(0x36, ProtocolVersion.MINECRAFT_1_20_2, true),
        mapping(0x38, ProtocolVersion.MINECRAFT_1_20_5, true),
        mapping(0x3A, ProtocolVersion.MINECRAFT_1_21_2, true),
        mapping(0x39, ProtocolVersion.MINECRAFT_1_21_5, true)
    )

    fun init() {
        Packet.of(PlayerAbilitiesPacket::class)
            .direction(Direction.CLIENTBOUND)
            .stateRegistry(StateRegistry.PLAY)
            .packetSupplier(::PlayerAbilitiesPacket)
            .mappings(MAPPINGS)
            .register()
        plugin.server.eventManager.register(plugin, this)
    }

    fun disable() {
        plugin.server.eventManager.unregisterListener(plugin, this)
    }

    @Subscribe
    fun onKickedFromServer(event: KickedFromServerEvent): EventTask? {
        if (event.kickedDuringServerConnect()) return null

        val reason = event.serverKickReason
            .orElse(Component.empty())
            .plainText

        if (!regex.matches(reason)) return null

        return EventTask.withContinuation { continuation ->
            Reconnection(event, continuation).reconnect()
        }
    }

    private inline val Component.plainText: String
        get() = serializer.serialize(this)
}
