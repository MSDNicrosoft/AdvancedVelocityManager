package work.msdnicrosoft.avm.command.utility

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.player
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.util.ConfigUtil
import work.msdnicrosoft.avm.util.ProxyServerUtil
import work.msdnicrosoft.avm.util.command.CommandUtil.buildHelper
import kotlin.jvm.optionals.getOrElse
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin as AVM

@Suppress("unused")
@PlatformSide(Platform.VELOCITY)
@CommandHeader(name = "send", permission = "avm.command.send")
object SendCommand {

    @CommandBody
    val main = mainCommand {
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
                        sendPlayer(
                            sender,
                            context.player("player"),
                            context["server"],
                            context["reason"]
                        )
                    }
                }
                execute<ProxyCommandSender> { sender, context, _ ->
                    val player = context.player("player")
                    val serverName = context["server"]
                    val serverNickname = ConfigUtil.getServerNickname(serverName)
                    sendPlayer(
                        sender,
                        player,
                        context["server"],
                        player.asLangText(
                            "command-send-target",
                            sender.name,
                            serverNickname
                        )
                    )
                }
            }
        }
        execute<ProxyCommandSender> { sender, _, argument ->
            buildHelper(this@SendCommand::class)
        }
    }

    private fun sendPlayer(sender: ProxyCommandSender, proxyPlayer: ProxyPlayer, serverName: String, reason: String) {
        val server = AVM.server.getServer(serverName).getOrElse {
            sender.sendLang("server-not-found", serverName)
            return
        }
        val serverNickname = ConfigUtil.getServerNickname(serverName)

        val playerName = proxyPlayer.name
        val player = AVM.server.getPlayer(playerName).getOrElse {
            sender.sendLang("player-not-found", playerName)
            return
        }

        ProxyServerUtil.sendPlayer(server, player).thenAccept { success ->
            if (success) {
                sender.sendLang(
                    "send-executor-success",
                    playerName,
                    serverNickname
                )
                proxyPlayer.sendMessage(reason)
            } else {
                sender.sendLang("command-send-executor-failed", playerName, serverNickname)
            }
        }
    }
}
