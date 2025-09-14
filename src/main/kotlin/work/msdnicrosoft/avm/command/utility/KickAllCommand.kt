package work.msdnicrosoft.avm.command.utility

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.CommandContext
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.command.data.component.MiniMessage
import work.msdnicrosoft.avm.util.component.hoverText
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.task

object KickAllCommand {
    val command = literalCommand("kickall") {
        requires { hasPermission("avm.command.kickall") }
        executes {
            server.allPlayers.filterNot { player ->
                player.hasPermission("avm.kickall.bypass")
            }.forEach { player ->
                player.disconnect(
                    tr(
                        "avm.command.avm.kick.target",
                        Argument.string("executor", context.source.name)
                    )
                )
            }
            Command.SINGLE_SUCCESS
        }
        wordArgument("server") {
            suggests { builder ->
                server.allPlayers.forEach { builder.suggest(it.username) }
                builder.buildFuture()
            }
            executes {
                val server: RegisteredServer by this
                kickAllPlayers(
                    server,
                    tr(
                        "avm.command.avm.kick.target",
                        Argument.string("executor", context.source.name)
                    )
                )
                Command.SINGLE_SUCCESS
            }
            stringArgument("reason") {
                executes {
                    val server: RegisteredServer by this
                    val reason: MiniMessage by this
                    kickAllPlayers(server, reason.component)
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }

    private fun CommandContext.kickAllPlayers(registeredServer: RegisteredServer, reason: Component) {
        if (registeredServer.playersConnected.isEmpty()) {
            sendTranslatable("avm.general.empty.server")
            return
        }

        task {
            val allPlayers: Collection<Player> = registeredServer.playersConnected
            val toKick: List<Player> = allPlayers.filterNot { player ->
                player.hasPermission("avm.kickall.bypass")
            }

            toKick.forEach { player ->
                player.disconnect(reason)
            }
            sendTranslatable(
                "avm.command.avm.kickall.executor.text",
                Argument.numeric("player_total", toKick.size),
                Argument.component(
                    "bypass",
                    tr(
                        "avm.command.avm.kickall.executor.bypass.text",
                        Argument.numeric("player_bypass", allPlayers.size - toKick.size)
                    ).hoverText(tr("avm.command.avm.kickall.executor.bypass.hover"))
                )
            )
        }
    }
}
