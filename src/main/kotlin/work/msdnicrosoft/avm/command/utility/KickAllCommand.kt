package work.msdnicrosoft.avm.command.utility

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.function.submitAsync
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.util.ProxyServerUtil
import kotlin.jvm.optionals.getOrElse
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin as AVM

@Suppress("unused")
@PlatformSide(Platform.VELOCITY)
@CommandHeader(name = "kickall", permission = "avm.command.kickall")
object KickAllCommand {

    @CommandBody
    val main = mainCommand {
        dynamic("server") {
            dynamic("reason") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    kickAllPlayers(sender, context["server"], context["reason"])
                }
            }
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                AVM.server.allServers.map { it.serverInfo.name }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                kickAllPlayers(sender, context["server"], sender.asLangText("kick-target", sender.name))
            }
        }
        execute<ProxyCommandSender> { sender, _, _ ->
            submitAsync(now = true) {
                ProxyServerUtil.kickPlayers(
                    sender.asLangText("command-kick-target", sender.name),
                    AVM.server.allPlayers.filterNot { it.hasPermission("avm.kickall.bypass") }
                )
            }
        }
    }

    /**
     * Kicks all players from a specific server.
     *
     * @param sender the command sender
     * @param server the name of the server to kick players from
     * @param reason the reason for the kick
     */
    private fun kickAllPlayers(sender: ProxyCommandSender, server: String, reason: String) {
        val server = AVM.server.getServer(server).getOrElse {
            sender.sendLang("server-not-found", server)
            return
        }

        if (server.playersConnected.isEmpty()) {
            sender.sendLang("general-empty-server")
            return
        }

        val (bypassed, playerToKick) = server.playersConnected
            .partition { it.hasPermission("avm.kickall.bypass") }

        submitAsync(now = true) { ProxyServerUtil.kickPlayers(reason, playerToKick) }

        sender.sendLang("command-kickall-executor", playerToKick.size, bypassed.size)
    }
}
