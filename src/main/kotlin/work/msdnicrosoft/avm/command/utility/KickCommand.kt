package work.msdnicrosoft.avm.command.utility

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.util.command.argument.ComponentArgumentType
import work.msdnicrosoft.avm.util.command.argument.PlayerArgumentType
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.task

object KickCommand {
    val command = literalCommand("kick") {
        requires { hasPermission("avm.command.kick") }
        argument("player", PlayerArgumentType.name()) {
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
            argument("reason", ComponentArgumentType.miniMessage()) {
                executes {
                    val player: Player by this
                    val reason: Component by this
                    task { player.disconnect(reason) }
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }
}
