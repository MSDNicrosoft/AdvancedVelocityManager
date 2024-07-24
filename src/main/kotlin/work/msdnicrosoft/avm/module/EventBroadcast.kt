package work.msdnicrosoft.avm.module

import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import net.kyori.adventure.text.Component
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submitAsync
import taboolib.module.chat.colored
import work.msdnicrosoft.avm.util.ConfigUtil
import work.msdnicrosoft.avm.util.Extensions.replace
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@PlatformSide(Platform.VELOCITY)
object EventBroadcast {
    @SubscribeEvent(postOrder = PostOrder.FIRST)
    fun onPlayerDisconnect(event: DisconnectEvent) {
        // If a player failed to join the server (due to incompatible server version, etc.),
        // plugin will send the leave message accidentally.
        // To avoid this, we check the login status.
        if (AVM.config.broadcast.leave.enabled && event.loginStatus == DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) {
            sendProxyServerMessage(AVM.config.broadcast.leave.message.replace("{player}", event.player.username))
        }
    }

    @SubscribeEvent(postOrder = PostOrder.FIRST)
    fun onPlayerConnected(event: ServerConnectedEvent) {
        val player = event.player
        val targetServerName = event.server.serverInfo.name
        val targetServerNickname = ConfigUtil.getServerNickname(targetServerName)

        event.previousServer.ifPresentOrElse(
            { previousServer ->
                if (AVM.config.broadcast.switch.enabled) {
                    val previousServerName = previousServer.serverInfo.name
                    val previousServerNickname = ConfigUtil.getServerNickname(previousServerName)
                    sendProxyServerMessage(
                        AVM.config.broadcast.switch.message.replace(
                            "{player}" to player.username,
                            "{previous_server_name}" to previousServerName,
                            "{previous_server_nickname}" to previousServerNickname,
                            "{target_server_nickname}" to targetServerNickname,
                            "{target_server_name}" to targetServerName
                        )
                    )
                }
            },
            {
                if (AVM.config.broadcast.join.enabled) {
                    sendProxyServerMessage(
                        AVM.config.broadcast.join.message.replace(
                            "{player}" to player.username,
                            "{server_name}" to targetServerName,
                            "{server_nickname}" to targetServerNickname
                        )
                    )
                }
            }
        )
    }

    private fun sendProxyServerMessage(message: String) = submitAsync {
        AVM.plugin.server.sendMessage(Component.text(message.colored()))
    }
}