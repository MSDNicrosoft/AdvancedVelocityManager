/**
 * Portions of this code are modified from lls-manager
 * https://github.com/plusls/lls-manager/blob/master/src/main/java/com/plusls/llsmanager/whitelist/WhitelistHandler.java
 */

package work.msdnicrosoft.avm.module.whitelist

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.InboundConnection
import com.velocitypowered.proxy.connection.client.InitialInboundConnection
import io.netty.util.AttributeKey
import org.geysermc.floodgate.api.player.FloodgatePlayer
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage

/**
 * Handles whitelist functionality for the server, including integration with Floodgate for player identification.
 * This object is specific to the Velocity platform and listens for pre-login and server pre-connect events
 * to enforce whitelist restrictions.
 */
object WhitelistHandler {

    private inline val config
        get() = ConfigManager.config.whitelist

    private val resolver by lazy {
        classOf<InboundConnection>().resolve()
            .firstField { name = "delegate" }
    }

    private val hasFloodgate by lazy { plugin.server.pluginManager.getPlugin("floodgate").isPresent }

    @Subscribe(order = PostOrder.EARLY)
    fun onPreLogin(event: PreLoginEvent) {
        // Blocked by other plugins or whitelist is off
        if (!event.result.isAllowed || !WhitelistManager.enabled) return

        val username = getUsername(event.username, event.connection)
        val player = WhitelistManager.getPlayer(username)
        if (player == null) {
            event.result = PreLoginEvent.PreLoginComponentResult.denied(miniMessage.deserialize(config.message))
            PlayerCache.add(username)
        } else {
            event.result = when (player.onlineMode) {
                true -> PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
                false -> PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
            }
        }
    }

    @Subscribe
    fun onPlayerLogin(event: LoginEvent) {
        WhitelistManager.updatePlayer(event.player.username, event.player.uniqueId)
    }

    @Subscribe(order = PostOrder.EARLY)
    fun onServerPreConnect(event: ServerPreConnectEvent) {
        // Blocked by other plugins or whitelist is off
        if (event.result.server.isEmpty || !WhitelistManager.enabled) return

        val serverName = event.originalServer.serverInfo.name
        val player = event.player

        if (!WhitelistManager.isInServerWhitelist(player.uniqueId, serverName)) {
            event.result = ServerPreConnectEvent.ServerResult.denied()
            val message = miniMessage.deserialize(config.message)
            player.sendMessage(message)
            if (event.previousServer == null) {
                player.disconnect(message)
            }
        }
    }

    /**
     * Retrieves the username for the player attempting to connect.
     * This method supports Floodgate integration to get the correct username for linked accounts.
     *
     * @param username The username of the player attempting to connect.
     * @param connection The InboundConnection associated with the player's connection.
     * @return The username or linked username if available.
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun getUsername(username: String, connection: InboundConnection): String {
        // Compatible with Floodgate
        if (hasFloodgate) {
            val channel = resolver.copy()
                .of(connection)
                .get<InitialInboundConnection>()!!.connection.channel
            val player: FloodgatePlayer? = channel
                .attr(AttributeKey.valueOf<FloodgatePlayer>("floodgate-player"))
                .get()
            if (player?.isLinked == true) {
                return player.linkedPlayer.javaUsername
            }
        }
        return username
    }
}
