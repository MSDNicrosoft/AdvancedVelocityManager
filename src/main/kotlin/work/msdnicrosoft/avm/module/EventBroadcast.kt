package work.msdnicrosoft.avm.module

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.submitAsync
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.string.formated
import work.msdnicrosoft.avm.util.string.replace

@PlatformSide(Platform.VELOCITY)
object EventBroadcast {

    private inline val config
        get() = ConfigManager.config.broadcast

    fun init() {
        plugin.server.eventManager.register(plugin, this)
    }

    fun disable() {
        plugin.server.eventManager.unregisterListener(plugin, this)
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onPlayerDisconnect(event: DisconnectEvent) {
        if (!config.leave.enabled) return

        // If a player failed to join the server (due to an incompatible server version, etc.),
        // the plugin will send the leave message accidentally.
        // To avoid this, we check the login status.
        if (event.loginStatus != DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) return

        sendMessage(config.leave.message.replace("%player_name%", event.player.username))

        if (config.leave.logging) {
            Logging.log("[-] ${event.player.username} left the server")
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onPlayerConnected(event: ServerConnectedEvent) {
        val username = event.player.username
        val targetServerName = event.server.serverInfo.name
        val targetServerNickname = getServerNickname(targetServerName)

        event.previousServer.ifPresentOrElse(
            { previousServer ->
                if (!config.switch.enabled) return@ifPresentOrElse

                val previousServerName = previousServer.serverInfo.name
                val previousServerNickname = getServerNickname(previousServerName)

                sendMessage(
                    config.switch.message.replace(
                        "%player_name%" to username,
                        "%previous_server_name%" to previousServerName,
                        "%previous_server_nickname%" to previousServerNickname,
                        "%target_server_nickname%" to targetServerNickname,
                        "%target_server_name%" to targetServerName
                    )
                )

                if (config.switch.logging) {
                    Logging.log("[⇄] $username: $previousServerName ➟ $targetServerName")
                }
            },
            {
                if (!config.join.enabled) return@ifPresentOrElse

                sendMessage(
                    config.join.message.replace(
                        "%player_name%" to username,
                        "%server_name%" to targetServerName,
                        "%server_nickname%" to targetServerNickname
                    )
                )

                if (config.join.logging) {
                    Logging.log("[+] $username joined server $targetServerName")
                }
            }
        )
    }

    private fun sendMessage(message: String) = submitAsync {
        plugin.server.allPlayers
            .parallelStream()
            .forEach { player ->
                player.sendMessage(message.formated())
            }
    }
}
