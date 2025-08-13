package work.msdnicrosoft.avm.module

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.eventManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.component.serializer.SerializationType.MINI_MESSAGE
import work.msdnicrosoft.avm.util.server.task

object EventBroadcast {

    private inline val config
        get() = ConfigManager.config.broadcast

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
            MINI_MESSAGE.deserialize(
                config.leave.message,
                Placeholder.unparsed("player_name", event.player.username)
            )
        )

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

                if (previousServer == event.server) return@ifPresentOrElse

                val previousServerName = previousServer.serverInfo.name
                val previousServerNickname = getServerNickname(previousServerName)

                sendMessage(
                    MINI_MESSAGE.deserialize(
                        config.switch.message,
                        Placeholder.unparsed("player_name", username),
                        Placeholder.unparsed("previous_server_name", previousServerName),
                        Placeholder.unparsed("previous_server_nickname", previousServerNickname),
                        Placeholder.unparsed("target_server_nickname", targetServerNickname),
                    )
                )

                if (config.switch.logging) {
                    Logging.log("[⇄] $username: $previousServerName ➟ $targetServerName")
                }
            },
            {
                if (!config.join.enabled) return@ifPresentOrElse

                sendMessage(
                    MINI_MESSAGE.deserialize(
                        config.join.message,
                        Placeholder.unparsed("player_name", username),
                        Placeholder.unparsed("server_name", targetServerName),
                        Placeholder.unparsed("server_nickname", targetServerNickname)
                    )
                )

                if (config.join.logging) {
                    Logging.log("[+] $username joined server $targetServerName")
                }
            }
        )
    }

    private fun sendMessage(message: Component) = task {
        plugin.server.allPlayers
            .parallelStream()
            .forEach { player -> player.sendMessage(message) }
    }
}
