/**
 * Portions of this code are modified from lls-manager
 * https://github.com/plusls/lls-manager/blob/master/src/main/java/com/plusls/llsmanager/minimapWorldSync/MinimapWorldSyncHandler.java
 */

package work.msdnicrosoft.avm.module.mapsync

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import io.netty.buffer.Unpooled
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.channelRegistrar
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.eventManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.net.netty.toByteArray
import work.msdnicrosoft.avm.util.net.netty.useThenApply
import java.nio.charset.StandardCharsets

object WorldInfoHandler {
    private val WORLD_INFO_CHANNEL: MinecraftChannelIdentifier =
        MinecraftChannelIdentifier.create("worldinfo", "world_id")

    private inline val config get() = ConfigManager.config.mapSync.worldInfo

    fun init() {
        channelRegistrar.register(this.WORLD_INFO_CHANNEL)
        eventManager.register(plugin, this)
    }

    fun disable() {
        channelRegistrar.unregister(this.WORLD_INFO_CHANNEL)
        eventManager.unregisterListener(plugin, this)
    }

    @Subscribe
    fun onPluginMessage(event: PluginMessageEvent) {
        if (!config.modern && !config.legacy) {
            return
        }
        if (event.identifier != this.WORLD_INFO_CHANNEL) {
            return
        }

        val player = event.source as? Player ?: return

        player.currentServer.ifPresent { connection ->
            val serverNameBytes: ByteArray = connection.serverInfo.name.toByteArray(StandardCharsets.UTF_8)
            if (config.modern) {
                player.sendPluginMessage(this.WORLD_INFO_CHANNEL, createArray(serverNameBytes, true))
            }
            if (config.legacy) {
                player.sendPluginMessage(this.WORLD_INFO_CHANNEL, createArray(serverNameBytes, false))
            }
        }

        if (config.modern || config.legacy) {
            event.result = PluginMessageEvent.ForwardResult.handled()
        }
    }

    @Suppress("MagicNumber")
    private fun createArray(serverNameBytes: ByteArray, modern: Boolean): ByteArray =
        Unpooled.buffer().useThenApply {
            writeByte(0x00) // Packet ID
            if (modern) writeByte(0x2A) // New packet
            writeByte(serverNameBytes.size)
            writeBytes(serverNameBytes)
            toByteArray()
        }
}
