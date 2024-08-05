package work.msdnicrosoft.avm.command.utility

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.util.ConfigUtil
import work.msdnicrosoft.avm.util.Extensions.sendMessage
import work.msdnicrosoft.avm.util.ProxyServerUtil
import work.msdnicrosoft.avm.util.command.CommandUtil.buildHelper
import kotlin.jvm.optionals.getOrElse
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin as AVM

@Suppress("unused")
@PlatformSide(Platform.VELOCITY)
@CommandHeader(name = "sendall", permission = "avm.command.sendall")
object SendAllCommand {

    @CommandBody
    val main = mainCommand {
        dynamic("server") {
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                AVM.server.allServers.map { it.serverInfo.name }
            }
            dynamic("reason") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    sendAllPlayers(sender, context["server"], context["reason"])
                }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                val reason = sender.asLangText(
                    "command-send-target",
                    sender.name,
                    ConfigUtil.getServerNickname(context["server"])
                )
                sendAllPlayers(sender, context["server"], reason)
            }
        }
        execute<ProxyCommandSender> { sender, context, _ ->
            buildHelper(this@SendAllCommand::class)
        }
    }

    /**
     * Sends all players to a specific server.
     *
     * @param sender the command sender
     * @param serverName the name of the server to send players to
     * @param reason the reason for the send
     */
    private fun sendAllPlayers(sender: ProxyCommandSender, serverName: String, reason: String) {
        val server = AVM.server.getServer(serverName).getOrElse {
            sender.sendLang("server-not-found", serverName)
            return
        }
        val serverNickname = ConfigUtil.getServerNickname(serverName)

        if (server.playersConnected.isEmpty()) {
            sender.sendLang("general-empty-server")
            return
        }

        val (bypassed, playerToSend) = AVM.server.allPlayers
            .filter { it.currentServer.get().serverInfo.name != serverName }
            .partition { it.hasPermission("avm.sendall.bypass") }

        val failedPlayers = buildList {
            playerToSend.forEach { player ->
                ProxyServerUtil.sendPlayer(server, player).thenAccept { success ->
                    if (success) {
                        player.sendMessage(reason)
                    } else {
                        add(player)
                    }
                }
            }
        }
        sender.sendLang(
            "command-sendall-executor",
            playerToSend.size - failedPlayers.size,
            serverNickname,
            bypassed.size,
            failedPlayers.size
        )
        if (!failedPlayers.isEmpty()) {
            sender.sendLang("command-sendall-executor-failed", failedPlayers.size, serverNickname)
        }
    }
}
