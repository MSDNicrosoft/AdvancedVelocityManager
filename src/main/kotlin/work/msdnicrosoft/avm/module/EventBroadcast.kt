package work.msdnicrosoft.avm.module

import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submitAsync
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.StringUtil.formated
import work.msdnicrosoft.avm.util.StringUtil.replace
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@PlatformSide(Platform.VELOCITY)
object EventBroadcast {

    private val config
        get() = ConfigManager.config.broadcast

    @SubscribeEvent(postOrder = PostOrder.FIRST)
    fun onPlayerDisconnect(event: DisconnectEvent) {
        if (!config.leave.enabled) return

        // If a player failed to join the server (due to incompatible server version, etc.),
        // plugin will send the leave message accidentally.
        // To avoid this, we check the login status.
        if (event.loginStatus != DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) return

        sendProxyServerMessage(config.leave.message.replace("%player_name%", event.player.username))
    }

    @SubscribeEvent(postOrder = PostOrder.FIRST)
    fun onPlayerConnected(event: ServerConnectedEvent) {
        val player = event.player
        val targetServerName = event.server.serverInfo.name
        val targetServerNickname = getServerNickname(targetServerName)

        event.previousServer.ifPresentOrElse(
            { previousServer ->
                if (!config.switch.enabled) return@ifPresentOrElse

                val previousServerName = previousServer.serverInfo.name
                val previousServerNickname = getServerNickname(previousServerName)
                sendProxyServerMessage(
                    config.switch.message.replace(
                        "%player_name%" to player.username,
                        "%previous_server_name%" to previousServerName,
                        "%previous_server_nickname%" to previousServerNickname,
                        "%target_server_nickname%" to targetServerNickname,
                        "%target_server_name%" to targetServerName
                    )
                )
            },
            {
                if (!config.join.enabled) return@ifPresentOrElse

                sendProxyServerMessage(
                    config.join.message.replace(
                        "%player_name%" to player.username,
                        "%server_name%" to targetServerName,
                        "%server_nickname%" to targetServerNickname
                    )
                )
            }
        )
    }

    private fun sendProxyServerMessage(message: String) = submitAsync {
        AVM.plugin.server.sendMessage(message.formated())
    }
}
