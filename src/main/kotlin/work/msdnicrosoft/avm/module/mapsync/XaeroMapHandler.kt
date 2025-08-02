/**
 * Portions of this code are modified from lls-manager
 * https://github.com/plusls/lls-manager/blob/master/src/main/java/com/plusls/llsmanager/minimapWorldSync/PlayerSpawnPosition.java
 */

package work.msdnicrosoft.avm.module.mapsync

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import com.velocitypowered.proxy.connection.MinecraftSessionHandler
import com.velocitypowered.proxy.connection.backend.BackendPlaySessionHandler
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection
import com.velocitypowered.proxy.protocol.MinecraftPacket
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction
import com.velocitypowered.proxy.protocol.StateRegistry
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.channelRegistrar
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.netty.use
import work.msdnicrosoft.avm.util.netty.useThenApply
import work.msdnicrosoft.avm.util.packet.MinecraftVersion
import work.msdnicrosoft.avm.util.packet.Packet
import java.nio.charset.StandardCharsets
import java.util.zip.CRC32

object XaeroMapHandler {
    private val XAERO_MINI_MAP_CHANNEL = MinecraftChannelIdentifier.create("xaerominimap", "main")
    private val XAERO_WORLD_MAP_CHANNEL = MinecraftChannelIdentifier.create("xaeroworldmap", "main")

    // https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol_version_numbers
    // https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Default_Spawn_Position
    @Suppress("MagicNumber")
    private val packet = Packet.of(SetDefaultSpawnPositionPacket::class)
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

    fun init() {
        packet.register()
        channelRegistrar.register(XAERO_WORLD_MAP_CHANNEL, XAERO_MINI_MAP_CHANNEL)
    }

    fun disable() {
        packet.unregister()
        channelRegistrar.unregister(XAERO_WORLD_MAP_CHANNEL, XAERO_MINI_MAP_CHANNEL)
    }

    class SetDefaultSpawnPositionPacket : MinecraftPacket {
        private var data: ByteBuf? = null

        override fun decode(buf: ByteBuf, direction: Direction?, protocolVersion: ProtocolVersion?) {
            data = buf.readBytes(buf.readableBytes())
        }

        override fun encode(buf: ByteBuf, direction: Direction?, protocolVersion: ProtocolVersion?) {
            data?.use { buf.writeBytes(data) }
        }

        @Suppress("UnsafeCallOnNullableType")
        override fun handle(sessionHandler: MinecraftSessionHandler): Boolean {
            if (!config.world && !config.mini) return true

            val connection = resolver.copy()
                .of(sessionHandler as BackendPlaySessionHandler)
                .get<VelocityServerConnection>()!!

            connection.player.connection.write(this)

            val serverNameBytes = connection.serverInfo.name.toByteArray(StandardCharsets.UTF_8)
            val worldId = CRC32().apply {
                update(serverNameBytes)
            }.value.toInt()
            val array = Unpooled.buffer().useThenApply {
                writeByte(0x00) // Packet ID
                writeInt(worldId) // World ID
                ByteBufUtil.getBytes(this)
            }

            if (config.world) {
                connection.player.sendPluginMessage(XAERO_WORLD_MAP_CHANNEL, array)
            }
            if (config.mini) {
                connection.player.sendPluginMessage(XAERO_MINI_MAP_CHANNEL, array)
            }

            return true
        }

        companion object {
            private val resolver = classOf<BackendPlaySessionHandler>().resolve()
                .firstField { name = "serverConn" }

            private inline val config
                get() = ConfigManager.config.mapSync.xaero
        }
    }
}
