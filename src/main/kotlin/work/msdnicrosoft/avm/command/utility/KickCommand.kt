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
import work.msdnicrosoft.avm.util.command.buildHelper
import kotlin.jvm.optionals.getOrElse
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin as AVMPlugin

@Suppress("unused")
@PlatformSide(Platform.VELOCITY)
@CommandHeader(name = "kick", permission = "avm.command.kick")
object KickCommand {
    @CommandBody
    val main = mainCommand {
        dynamic("player") {
            dynamic("reason") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    kickPlayer(sender, context["player"], context["reason"])
                }
            }
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                AVMPlugin.server.allPlayers.map { it.username }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                kickPlayer(sender, context["player"], sender.asLangText("kick-target-feedback", sender.name))
            }
        }
        execute<ProxyCommandSender> { sender, context, argument ->
            buildHelper(this@KickCommand::class)
        }
    }

    private fun kickPlayer(sender: ProxyCommandSender, player: String, reason: String) {
        val playerToKick = AVMPlugin.server.getPlayer(player).getOrElse {
            sender.sendLang("player-not-found", player)
            return
        }
        submitAsync(now = true) { ProxyServerUtil.kickPlayers(reason, playerToKick) }
    }
}
