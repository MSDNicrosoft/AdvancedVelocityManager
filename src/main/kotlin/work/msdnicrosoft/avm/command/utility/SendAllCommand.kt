package work.msdnicrosoft.avm.command.utility

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
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
                val reason = sender.asLangText(
                    "command-send-target",
                    sender.name,
                    getServerNickname(context["server"])
                )
                sender.sendAllPlayers(context["server"], reason)
            }
        }
    }

    /**
     * Sends all players to a specific server.
     *
     * @param serverName the name of the server to send players to
     * @param reason the reason for the send
     */
    private fun ProxyCommandSender.sendAllPlayers(serverName: String, reason: String) {
        val server = AVM.server.getServer(serverName).getOrElse {
            sendLang("server-not-found", serverName)
            return
        }
        val serverNickname = getServerNickname(serverName)

        if (server.playersConnected.isEmpty()) {
            sendLang("general-empty-server")
            return
        }

        val (bypassed, playerToSend) = AVM.server.allPlayers
            .filter { it.currentServer.get().serverInfo.name != serverName }
            .partition { it.hasPermission("avm.sendall.bypass") }

        val failedPlayers = buildList {
            playerToSend.forEach { player ->
                sendPlayer(server, player).thenAccept { success ->
                    if (success) {
                        player.sendMessage(reason)
                    } else {
                        add(player)
                    }
                }
            }
        }
        sendLang(
            "command-sendall-executor",
            playerToSend.size - failedPlayers.size,
            serverNickname,
            bypassed.size,
            failedPlayers.size
        )
        if (!failedPlayers.isEmpty()) {
            sendLang("command-sendall-executor-failed", failedPlayers.size, serverNickname)
        }
    }
}
