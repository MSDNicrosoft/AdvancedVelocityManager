package work.msdnicrosoft.avm.packet

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.highcapable.kavaref.resolver.FieldResolver
import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.proxy.connection.MinecraftSessionHandler
import com.velocitypowered.proxy.connection.backend.BackendPlaySessionHandler
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection
import com.velocitypowered.proxy.protocol.MinecraftPacket
import com.velocitypowered.proxy.protocol.ProtocolUtils
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.mapsync.XaeroMapHandler
import work.msdnicrosoft.avm.util.net.netty.toByteArray
import work.msdnicrosoft.avm.util.net.netty.use
import work.msdnicrosoft.avm.util.net.netty.useApply
import java.nio.charset.StandardCharsets
import java.util.zip.CRC32

class SetDefaultSpawnPositionPacket : MinecraftPacket {
    private var data: ByteBuf? = null

    override fun decode(buf: ByteBuf, direction: ProtocolUtils.Direction?, protocolVersion: ProtocolVersion?) {
        this.data = buf.readBytes(buf.readableBytes())
    }

    override fun encode(buf: ByteBuf, direction: ProtocolUtils.Direction?, protocolVersion: ProtocolVersion?) {
        this.data?.use { buf.writeBytes(this.data) }
    }

    @Suppress("UnsafeCallOnNullableType")
    override fun handle(sessionHandler: MinecraftSessionHandler): Boolean {
        if (!config.world && !config.mini) return true

        val connection: VelocityServerConnection = SERVER_CONN_FIELD_RESOLVER.copy()
            .of(sessionHandler as BackendPlaySessionHandler)
            .get<VelocityServerConnection>()!!

        connection.player.connection.write(this)

        val serverNameBytes: ByteArray = connection.serverInfo.name.toByteArray(StandardCharsets.UTF_8)
        val worldId: Int = CRC32().apply {
            update(serverNameBytes)
        }.value.toInt()
        val array: ByteArray = Unpooled.buffer().useApply {
            writeByte(0x00) // Packet ID
            writeInt(worldId) // World ID
        }.toByteArray()

        if (config.world) {
            connection.player.sendPluginMessage(XaeroMapHandler.XAERO_WORLD_MAP_CHANNEL, array)
        }
        if (config.mini) {
            connection.player.sendPluginMessage(XaeroMapHandler.XAERO_MINI_MAP_CHANNEL, array)
        }

        return true
    }

    companion object {
        private val SERVER_CONN_FIELD_RESOLVER: FieldResolver<BackendPlaySessionHandler> = classOf<BackendPlaySessionHandler>().resolve()
            .firstField { name = "serverConn" }

        private inline val config get() = ConfigManager.config.mapSync.xaero
    }
}
