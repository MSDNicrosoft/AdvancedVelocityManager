package work.msdnicrosoft.avm.module

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.Component
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.eventManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.component.builder.minimessage.miniMessage
import work.msdnicrosoft.avm.util.server.nickname
import work.msdnicrosoft.avm.util.server.task

object EventBroadcast {
    private inline val config get() = ConfigManager.config.broadcast

    fun init() {
        eventManager.register(plugin, this)
    }

    fun disable() {
        eventManager.unregisterListener(plugin, this)
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onPlayerDisconnect(event: DisconnectEvent) {
        if (!config.leave.enabled) return

        // If a player failed to join the server (due to an incompatible server version, etc.),
        // the plugin will send the leave message accidentally.
        // To avoid this, we check the login status.
        if (event.loginStatus != DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) return

        sendMessage(
            miniMessage(config.leave.message) {
                placeholders { unparsed("player_name", event.player.username) }
            }
        )

        if (config.leave.logging) {
            Logging.log("[-] ${event.player.username} left the server")
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onPlayerConnected(event: ServerConnectedEvent) {
        val username: String = event.player.username
        val targetServerName: String = event.server.serverInfo.name
        val targetServerNickname: String = event.server.serverInfo.nickname

        event.previousServer.ifPresentOrElse(
            { previousServer: RegisteredServer ->
                if (!config.switch.enabled) return@ifPresentOrElse

                if (previousServer == event.server) return@ifPresentOrElse

                val previousServerName: String = previousServer.serverInfo.name
                val previousServerNickname: String = previousServer.serverInfo.nickname

                this.sendMessage(
                    miniMessage(config.switch.message) {
                        placeholders {
                            unparsed("player_name", username)
                            unparsed("previous_server_name", previousServerName)
                            unparsed("previous_server_nickname", previousServerNickname)
                            unparsed("target_server_nickname", targetServerNickname)
                        }
                    }
                )

                if (config.switch.logging) {
                    Logging.log("[⇄] $username: $previousServerName ➟ $targetServerName")
                }
            },
            {
                if (!config.join.enabled) return@ifPresentOrElse

                this.sendMessage(
                    miniMessage(config.join.message) {
                        placeholders {
                            unparsed("player_name", username)
                            unparsed("server_name", targetServerName)
                            unparsed("server_nickname", targetServerNickname)
                        }
                    }
                )

                if (config.join.logging) {
                    Logging.log("[+] $username joined server $targetServerName")
                }
            }
        )
    }

    private fun sendMessage(message: Component) = task {
        plugin.server.allPlayers.parallelStream()
            .forEach { player -> player.sendMessage(message) }
    }
}
