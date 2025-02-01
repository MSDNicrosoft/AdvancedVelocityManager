package work.msdnicrosoft.avm.command.utility

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.player
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.ProxyServerUtil.getRegisteredServer
import work.msdnicrosoft.avm.util.ProxyServerUtil.sendPlayer
import kotlin.jvm.optionals.getOrElse
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin as AVM

@PlatformSide(Platform.VELOCITY)
object SendCommand {

    val command = subCommand {
        player("player") {
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                AVM.server.allPlayers.map { it.username }
            }
            dynamic("server") {
                suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                    AVM.server.allServers.map { it.serverInfo.name }
                }
                dynamic("reason") {
                    execute<ProxyCommandSender> { sender, context, _ ->
                        sender.sendPlayer(context.player("player"), context["server"], context["reason"])
                    }
                }
                execute<ProxyCommandSender> { sender, context, _ ->
                    val player = context.player("player")
                    val serverName = context["server"]
                    val serverNickname = getServerNickname(serverName)
                    val reason = player.asLangText(
                        "command-send-target",
                        sender.name,
                        serverNickname
                    )
                    sender.sendPlayer(player, serverName, reason)
                }
            }
        }
    }

    private fun ProxyCommandSender.sendPlayer(proxyPlayer: ProxyPlayer, serverName: String, reason: String) {
        val server = getRegisteredServer(serverName).getOrElse {
            sendLang("server-not-found", serverName)
            return
        }
        val serverNickname = getServerNickname(serverName)

        val playerName = proxyPlayer.name
        val player = getPlayer(playerName).getOrElse {
            sendLang("player-not-found", playerName)
            return
        }

        sendPlayer(server, player).thenAccept { success ->
            if (success) {
                sendLang("send-executor-success", playerName, serverNickname)
                proxyPlayer.sendMessage(reason)
            } else {
                sendLang("command-send-executor-failed", playerName, serverNickname)
            }
        }
    }
}
