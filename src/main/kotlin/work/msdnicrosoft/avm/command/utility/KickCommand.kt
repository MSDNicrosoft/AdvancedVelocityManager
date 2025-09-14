package work.msdnicrosoft.avm.command.utility

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.command.data.component.MiniMessage
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.task

object KickCommand {
    val command = literalCommand("kick") {
        requires { hasPermission("avm.command.kick") }
        wordArgument("player") {
            suggests { builder ->
                server.allPlayers.forEach { builder.suggest(it.username) }
                builder.buildFuture()
            }
            executes {
                val player: Player by this

                task {
                    player.disconnect(
                        tr(
                            "avm.command.avm.kick.target",
                            Argument.string("executor", context.source.name)
                        )
                    )
                }
                Command.SINGLE_SUCCESS
            }
            stringArgument("reason") {
                executes {
                    val player: Player by this
                    val reason: MiniMessage by this
                    task { player.disconnect(reason.component) }
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }
}
