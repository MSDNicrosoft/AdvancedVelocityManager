/**
 * Portions of this code are from lls-manager
 * https://github.com/plusls/lls-manager/blob/master/src/main/java/com/plusls/llsmanager/whitelist/WhitelistHandler.java
 */

package work.msdnicrosoft.avm.module.whitelist

import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.InboundConnection
import io.netty.channel.Channel
import io.netty.util.AttributeKey
import org.geysermc.floodgate.api.player.FloodgatePlayer
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.warning
import taboolib.module.lang.asLangText
import work.msdnicrosoft.avm.impl.VelocityConsole
import work.msdnicrosoft.avm.util.Extensions.formated
import work.msdnicrosoft.avm.util.Extensions.sendMessage
import java.lang.reflect.Field
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

/**
 * Handles whitelist functionality for the server, including integration with Floodgate for player identification.
 * This object is specific to the Velocity platform and listens for pre-login and server pre-connect events
 * to enforce whitelist restrictions.
 */
@Suppress("unused")
@PlatformSide(Platform.VELOCITY)
object WhitelistHandler {

    // Reflection fields for accessing internal Velocity connection details
    private lateinit var INITIAL_MINECRAFT_CONNECTION: Field
    private lateinit var CHANNEL: Field
    private lateinit var DELEGATE: Field

    /**
     * Initializes reflection fields used to access internal Velocity connection details.
     * This method is called at the CONST lifecycle phase of plugin loading.
     */
    @Awake(LifeCycle.CONST)
    private fun init() {
        runCatching {
            INITIAL_MINECRAFT_CONNECTION =
                Class.forName("com.velocitypowered.proxy.connection.client.InitialInboundConnection")
                    .getDeclaredField("connection")
                    .apply { trySetAccessible() }

            CHANNEL = Class.forName("com.velocitypowered.proxy.connection.MinecraftConnection")
                .getDeclaredField("channel")
                .apply { trySetAccessible() }

            DELEGATE = Class.forName("com.velocitypowered.proxy.connection.client.LoginInboundConnection")
                .getDeclaredField("delegate")
                .apply { trySetAccessible() }
        }.onFailure {
            it.printStackTrace()
            error("Failed to initialize Floodgate hook")
        }
    }

    @SubscribeEvent(postOrder = PostOrder.EARLY)
    fun onPreLogin(event: PreLoginEvent) {
        // Blocked by other plugins
        if (!event.result.isAllowed) return

        // Whitelist is off
        if (!AVM.config.whitelist.enabled) return

        val username = getUsername(event.username, event.connection)
        if (!WhitelistManager.isWhitelisted(username)) {
            event.result = PreLoginEvent.PreLoginComponentResult
                .denied(VelocityConsole.asLangText("whitelist-not-whitelisted").formated())
        }
    }

    @SubscribeEvent(postOrder = PostOrder.EARLY)
    fun onServerPreConnect(event: ServerPreConnectEvent) {
        // Blocked by other plugins
        if (event.result.server.isEmpty) return

        // Whitelist is off
        if (!AVM.config.whitelist.enabled) return

        val serverName = event.result.server.get().serverInfo.name
        val player = event.player
        if (!WhitelistManager.isWhitelisted(player.username)) {
            event.result = ServerPreConnectEvent.ServerResult.denied()
            player.sendMessage(VelocityConsole.asLangText("whitelist-not-whitelisted"))
        }
    }

    /**
     * Retrieves the username for the player attempting to connect. This method supports Floodgate integration
     * to obtain the correct username for linked accounts.
     *
     * @param username The username of the player attempting to connect.
     * @param connection The InboundConnection associated with the player's connection.
     * @return The username or linked username if available.
     */
    private fun getUsername(username: String, connection: InboundConnection): String {
        // Compatible with Floodgate
        if (AVM.hasFloodgate) {
            runCatching {
                val minecraftConnection: Any = INITIAL_MINECRAFT_CONNECTION[DELEGATE[connection]]
                val channel = CHANNEL[minecraftConnection] as Channel

                val player = channel.attr(AttributeKey.valueOf<Any>("floodgate-player")).get() as FloodgatePlayer?
                if (player?.isLinked == true) {
                    return player.linkedPlayer.javaUsername
                }
            }.onFailure {
                warning("An error occurred while processing floodgate player")
                it.printStackTrace()
            }
        }
        return username
    }
}
