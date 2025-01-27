/**
 * Portions of this code are modified from lls-manager
 * https://github.com/plusls/lls-manager/blob/master/src/main/java/com/plusls/llsmanager/whitelist/WhitelistHandler.java
 */

package work.msdnicrosoft.avm.module.whitelist

import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.InboundConnection
import com.velocitypowered.proxy.connection.MinecraftConnection
import com.velocitypowered.proxy.connection.client.InitialInboundConnection
import io.netty.channel.Channel
import io.netty.util.AttributeKey
import org.geysermc.floodgate.api.player.FloodgatePlayer
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.reflex.Reflex.Companion.getProperty
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.logger
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.StringUtil.formated
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

/**
 * Handles whitelist functionality for the server, including integration with Floodgate for player identification.
 * This object is specific to the Velocity platform and listens for pre-login and server pre-connect events
 * to enforce whitelist restrictions.
 */
@PlatformSide(Platform.VELOCITY)
object WhitelistHandler {

    private val config
        get() = ConfigManager.config.whitelist

    private val hasFloodgate by lazy { AVM.plugin.server.pluginManager.getPlugin("floodgate").isPresent }

    @SubscribeEvent(postOrder = PostOrder.EARLY)
    fun onPreLogin(event: PreLoginEvent) {
        // Blocked by other plugins
        if (!event.result.isAllowed) return

        // Whitelist is off
        if (WhitelistManager.state == WhitelistManager.WhitelistState.OFF) return

        val username = getUsername(event.username, event.connection)
        if (!WhitelistManager.isInWhitelist(username)) {
            event.result = PreLoginEvent.PreLoginComponentResult.denied(config.message.formated())
            PlayerCache.add(username)
            return
        }

        if (event.result.isAllowed) {
            when (WhitelistManager.getPlayer(username)?.onlineMode) {
                true -> event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
                false -> event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
                null -> {}
            }
        }
    }

    @SubscribeEvent
    fun onPlayerLogin(event: LoginEvent) {
        WhitelistManager.updatePlayer(event.player.username, event.player.uniqueId)
    }

    @SubscribeEvent(postOrder = PostOrder.EARLY)
    fun onServerPreConnect(event: ServerPreConnectEvent) {
        // Blocked by other plugins
        if (event.result.server.isEmpty) return

        // Whitelist is off
        if (WhitelistManager.state == WhitelistManager.WhitelistState.OFF) return

        val serverName = event.originalServer.serverInfo.name
        val player = event.player

        if (!WhitelistManager.isInServerWhitelist(player.uniqueId, serverName)) {
            event.result = ServerPreConnectEvent.ServerResult.denied()
            val message = config.message.formated()
            player.sendMessage(message)
            if (event.previousServer == null) {
                player.disconnect(message)
            }
        }
    }

    /**
     * Retrieves the username for the player attempting to connect.
     * This method supports Floodgate integration to obtain the correct username for linked accounts.
     *
     * @param username The username of the player attempting to connect.
     * @param connection The InboundConnection associated with the player's connection.
     * @return The username or linked username if available.
     */
    private fun getUsername(username: String, connection: InboundConnection): String {
        // Compatible with Floodgate
        if (hasFloodgate) {
            try {
                val channel = connection.getProperty<InitialInboundConnection>("delegate")
                    ?.getProperty<MinecraftConnection>("connection")
                    ?.getProperty<Channel>("channel")
                val player = channel?.attr(AttributeKey.valueOf<FloodgatePlayer>("floodgate-player"))?.get()
                if (player?.isLinked == true) {
                    return player.linkedPlayer.javaUsername
                }
            } catch (e: IllegalAccessException) {
                logger.error("Failed to process Floodgate player", e)
            }
        }
        return username
    }
}
