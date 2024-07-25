package work.msdnicrosoft.avm.command.utility

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.player
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.util.ConfigUtil
import work.msdnicrosoft.avm.util.ProxyServerUtil
import kotlin.jvm.optionals.getOrElse
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin as AVMPlugin

@Suppress("unused")
@PlatformSide(Platform.VELOCITY)
@CommandHeader(name = "send", permission = "avm.command.send")
object SendCommand {

    @CommandBody
    val main = mainCommand {
        player("player") {
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                AVMPlugin.server.allPlayers.map { it.username }
            }
            dynamic("server") {
                dynamic("reason") {

                }
                suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                    AVMPlugin.server.allServers.map { it.serverInfo.name }
                }
                execute<ProxyCommandSender> { sender, context, _ ->
                    val serverName = context["server"]
                    val server = AVMPlugin.server.getServer(serverName).getOrElse {
                        sender.sendLang("server-not-found", serverName)
                        return@execute
                    }
                    val serverNickname = ConfigUtil.getServerNickname(serverName)

                    val playerName = context.player("player").name
                    val player = AVMPlugin.server.getPlayer(playerName).getOrElse {
                        sender.sendLang("player-not-found", playerName)
                        return@execute
                    }

                    ProxyServerUtil.sendPlayer(server, player).thenAccept { success ->
                        if (success) {
                            sender.sendLang(
                                "send-executor-feedback",
                                playerName,
                                serverNickname
                            )
                            context.player("player").sendLang("send-target-feedback", sender.name, serverNickname)
                        } else {
                            sender.sendLang("send-executor-failed", playerName, serverNickname)
                        }
                    }
                }
            }
        }
        execute<ProxyCommandSender> { sender, _, argument ->
            TODO()
        }
    }
}
