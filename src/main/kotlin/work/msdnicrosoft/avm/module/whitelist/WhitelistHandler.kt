/**
 * Portions of this code are modified from lls-manager
 * https://github.com/plusls/lls-manager/blob/master/src/main/java/com/plusls/llsmanager/whitelist/WhitelistHandler.java
 */

package work.msdnicrosoft.avm.module.whitelist

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.highcapable.kavaref.resolver.FieldResolver
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.InboundConnection
import com.velocitypowered.proxy.connection.client.InitialInboundConnection
import io.netty.channel.Channel
import io.netty.util.AttributeKey
import net.kyori.adventure.text.Component
import org.geysermc.floodgate.api.player.FloodgatePlayer
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.component.ComponentSerializer.MINI_MESSAGE

object WhitelistHandler {
    private inline val config get() = ConfigManager.config.whitelist

    private val delegateFieldResolver: FieldResolver<InboundConnection> by lazy {
        classOf<InboundConnection>().resolve().firstField { name = "delegate" }
    }

    private val hasFloodgate: Boolean by lazy { plugin.server.pluginManager.getPlugin("floodgate").isPresent }

    @Subscribe(order = PostOrder.EARLY)
    fun onPreLogin(event: PreLoginEvent) {
        // Blocked by other plugins or whitelist is off
        if (!event.result.isAllowed || !WhitelistManager.enabled) return

        val username: String = event.connection.getJavaUsernameOrDefault(event.username)
        val player = WhitelistManager.getPlayer(username)
        if (player == null) {
            event.result = PreLoginEvent.PreLoginComponentResult.denied(MINI_MESSAGE.deserialize(config.message))
            PlayerCache.add(username)
        } else {
            event.result = if (player.onlineMode) {
                PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
            } else {
                PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
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

        val serverName: String = event.originalServer.serverInfo.name
        val player = event.player

        if (!WhitelistManager.isInServerWhitelist(player.uniqueId, serverName)) {
            event.result = ServerPreConnectEvent.ServerResult.denied()
            val message: Component = MINI_MESSAGE.deserialize(config.message)
            player.sendMessage(message)
            if (event.previousServer == null) {
                player.disconnect(message)
            }
        }
    }

    /**
     * Retrieves the Java username associated with the connection when Floodgate is enabled and the user is linked.
     * If Floodgate is not enabled or the user is not linked, returns the provided default [username].
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun InboundConnection.getJavaUsernameOrDefault(username: String): String {
        // Compatible with Floodgate
        if (this@WhitelistHandler.hasFloodgate) {
            val channel: Channel = this@WhitelistHandler.delegateFieldResolver.copy()
                .of(this)
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
