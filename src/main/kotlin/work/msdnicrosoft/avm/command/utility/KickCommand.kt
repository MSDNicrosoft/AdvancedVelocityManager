package work.msdnicrosoft.avm.command.utility

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submitAsync
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.util.ProxyServerUtil.kickPlayers
import kotlin.jvm.optionals.getOrElse
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin as AVM

@PlatformSide(Platform.VELOCITY)
object KickCommand {
    val command = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                AVM.server.allPlayers.map { it.username }
            }
            dynamic("reason") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    sender.kickPlayer(context["player"], context["reason"])
                }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                sender.kickPlayer(context["player"], sender.asLangText("command-kick-target", sender.name))
            }
        }
    }

    private fun ProxyCommandSender.kickPlayer(player: String, reason: String) {
        val playerToKick = AVM.server.getPlayer(player).getOrElse {
            sendLang("player-not-found", player)
            return
        }
        submitAsync(now = true) { kickPlayers(reason, playerToKick) }
    }
}
