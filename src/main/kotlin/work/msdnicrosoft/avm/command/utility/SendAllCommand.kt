package work.msdnicrosoft.avm.command.utility

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submitAsync
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.ProxyServerUtil.getRegisteredServer
import work.msdnicrosoft.avm.util.ProxyServerUtil.sendMessage
import work.msdnicrosoft.avm.util.ProxyServerUtil.sendPlayer
import kotlin.jvm.optionals.getOrElse
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin as AVM

@PlatformSide(Platform.VELOCITY)
object SendAllCommand {

    val command = subCommand {
        dynamic("server") {
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                AVM.server.allServers.map { it.serverInfo.name }
            }
            dynamic("reason") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    sender.sendAllPlayers(context["server"], context["reason"])
                }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                val serverName = context["server"]
                val reason = sender.asLangText(
                    "command-send-target",
                    sender.name,
                    getServerNickname(serverName)
                )
                sender.sendAllPlayers(serverName, reason)
            }
        }
    }

    private fun ProxyCommandSender.sendAllPlayers(serverName: String, reason: String) {
        val server = getRegisteredServer(serverName).getOrElse {
            sendLang("server-not-found", serverName)
            return
        }
        val serverNickname = getServerNickname(serverName)

        if (server.playersConnected.isEmpty()) {
            sendLang("general-empty-server")
            return
        }

        val (bypassed, toSend) = AVM.server.allPlayers
            .filter { it.currentServer.get().serverInfo.name != serverName }
            .partition { it.hasPermission("avm.sendall.bypass") }

        submitAsync(now = true) {
            toSend.forEach { player ->
                sendPlayer(server, player).thenAcceptAsync { success ->
                    if (success) {
                        player.sendMessage(reason)
                    }
                }
            }

            sendLang(
                "command-sendall-executor",
                toSend.size,
                serverNickname,
                bypassed.size
            )
        }
    }
}
