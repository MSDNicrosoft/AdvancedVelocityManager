package work.msdnicrosoft.avm.command.utility

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.server.task
import kotlin.jvm.optionals.getOrElse

object KickCommand {

    val command = literalCommand("kick") {
        requires { hasPermission("avm.command.kick") }
        wordArgument("player") {
            suggests { builder ->
                server.allPlayers.forEach { builder.suggest(it.username) }
                builder.buildFuture()
            }
            executes {
                val player: String by this

                context.source.kickPlayer(
                    player,
                    tr("avm.command.avm.kick.target", Argument.string("executor", context.source.name))
                )
                Command.SINGLE_SUCCESS
            }
            wordArgument("reason") {
                executes {
                    val player: String by this
                    val reason: String by this
                    context.source.kickPlayer(player, miniMessage.deserialize(reason))
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }

    private fun CommandSource.kickPlayer(player: String, reason: Component) {
        val playerToKick = getPlayer(player).getOrElse {
            this.sendTranslatable("avm.general.not.exist.player", Argument.string("player", player))
            return
        }
        task { playerToKick.disconnect(reason) }
    }
}
