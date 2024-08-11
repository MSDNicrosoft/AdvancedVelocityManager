/**
 * Portions of this code are from lls-manager
 * https://github.com/plusls/lls-manager/blob/master/src/main/java/com/plusls/llsmanager/whitelist/WhitelistHandler.java
 */

package work.msdnicrosoft.avm.module.whitelist

import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.InboundConnection
import com.velocitypowered.proxy.connection.MinecraftConnection
import com.velocitypowered.proxy.connection.client.InitialInboundConnection
import com.velocitypowered.proxy.connection.client.LoginInboundConnection
import io.netty.channel.Channel
import io.netty.util.AttributeKey
import org.geysermc.floodgate.api.player.FloodgatePlayer
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.SubscribeEvent
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.logger
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.ReflectUtil
import work.msdnicrosoft.avm.util.StringUtil.formated
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

/**
 * Handles whitelist functionality for the server, including integration with Floodgate for player identification.
 * This object is specific to the Velocity platform and listens for pre-login and server pre-connect events
 * to enforce whitelist restrictions.
 */
@Suppress("unused")
@PlatformSide(Platform.VELOCITY)
object WhitelistHandler {

    private val config
        get() = ConfigManager.config.whitelist

    // Reflection fields for accessing internal Velocity connection details
    private val INITIAL_MINECRAFT_CONNECTION = ReflectUtil.getField(InitialInboundConnection::class.java, "connection")
    private val CHANNEL = ReflectUtil.getField(MinecraftConnection::class.java, "channel")
    private val DELEGATE = ReflectUtil.getField(LoginInboundConnection::class.java, "delegate")

    @SubscribeEvent(postOrder = PostOrder.EARLY)
    fun onPreLogin(event: PreLoginEvent) {
        // Blocked by other plugins
        if (!event.result.isAllowed) return

        // Whitelist is off
        if (WhitelistManager.state == WhitelistManager.WhitelistState.OFF) return

        val username = getUsername(event.username, event.connection)
        if (!WhitelistManager.isInWhitelist(username)) {
            event.result = PreLoginEvent.PreLoginComponentResult.denied(config.message.formated())
            return
        }

        if (event.result == PreLoginEvent.PreLoginComponentResult.allowed()) {
            event.result = if (WhitelistManager.getPlayer(event.username)?.onlineMode == true) {
                PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
            } else {
                PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
            }
        }
    }

    @SubscribeEvent
    fun onPlayerLogin(event: LoginEvent) = WhitelistManager.updatePlayer(event.player.username, event.player.uniqueId)

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
        if (AVM.hasFloodgate) {
            try {
                val channel = CHANNEL[INITIAL_MINECRAFT_CONNECTION[DELEGATE[connection]]] as Channel?

                val player = channel?.attr(AttributeKey.valueOf<Any>("floodgate-player"))?.get() as FloodgatePlayer?
                if (player?.isLinked == true) {
                    return player.linkedPlayer.javaUsername
                }
            } catch (e: Exception) {
                logger.error("Failed to process Floodgate player", e)
            }
        }
        return username
    }
}
