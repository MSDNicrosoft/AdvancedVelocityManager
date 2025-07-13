/**
 * Portions of this code are modified from lls-manager
 * https://github.com/plusls/lls-manager/blob/master/src/main/java/com/plusls/llsmanager/minimapWorldSync/MinimapWorldSyncHandler.java
 */

package work.msdnicrosoft.avm.module.mapsync

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import java.nio.charset.StandardCharsets

object VoxelMapHandler {
    private val VOXEL_CHANNEL = MinecraftChannelIdentifier.create("worldinfo", "world_id")

    fun init() {
        plugin.server.channelRegistrar.register(VOXEL_CHANNEL)
        plugin.server.eventManager.register(plugin, this)
    }

    fun disable() {
        plugin.server.channelRegistrar.unregister(VOXEL_CHANNEL)
        plugin.server.eventManager.unregisterListener(plugin, this)
    }

    @Subscribe
    fun onPluginMessage(event: PluginMessageEvent) {
        if (event.source !is Player || event.identifier != VOXEL_CHANNEL) return
        val player = event.source as Player

        player.currentServer.ifPresent { serverConnection ->
            val serverName = serverConnection.serverInfo.name
            player.sendPluginMessage(VOXEL_CHANNEL, createArray(serverName, true))
            player.sendPluginMessage(VOXEL_CHANNEL, createArray(serverName, false))
        }

        event.result = PluginMessageEvent.ForwardResult.handled()
    }

    @Suppress("MagicNumber")
    private fun createArray(serverName: String, modern: Boolean): ByteArray {
        val serverNameBytes = serverName.toByteArray(StandardCharsets.UTF_8)
        val buf = Unpooled.buffer().apply {
            writeByte(0x00)
            if (modern) writeByte(0x2A)
            writeByte(serverNameBytes.size)
            writeBytes(serverNameBytes)
        }
        val array = ByteBufUtil.getBytes(buf)
        buf.release()
        return array
    }
}
