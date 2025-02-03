package work.msdnicrosoft.avm.command.utility

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submitAsync
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.util.ProxyServerUtil.getRegisteredServer
import work.msdnicrosoft.avm.util.ProxyServerUtil.kickPlayers
import kotlin.jvm.optionals.getOrElse
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@PlatformSide(Platform.VELOCITY)
object KickAllCommand {
    val command = subCommand {
        dynamic("server") {
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                AVM.plugin.server.allServers.map { it.serverInfo.name }
            }
            dynamic("reason") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    sender.kickAllPlayers(context["server"], context["reason"])
                }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                sender.kickAllPlayers(context["server"], sender.asLangText("command-kick-target", sender.name))
            }
        }
        execute<ProxyCommandSender> { sender, _, _ ->
            submitAsync(now = true) {
                kickPlayers(
                    sender.asLangText("command-kick-target", sender.name),
                    AVM.plugin.server.allPlayers.filter { !it.hasPermission("avm.kickall.bypass") }
                )
            }
        }
    }

    private fun ProxyCommandSender.kickAllPlayers(serverName: String, reason: String) {
        val server = getRegisteredServer(serverName).getOrElse {
            sendLang("server-not-found", serverName)
            return
        }

        if (server.playersConnected.isEmpty()) {
            sendLang("general-empty-server")
            return
        }

        val (bypassed, toKick) = server.playersConnected.partition { it.hasPermission("avm.kickall.bypass") }

        submitAsync(now = true) { kickPlayers(reason, toKick) }

        sendLang("command-kickall-executor", toKick.size, bypassed.size)
    }
}
