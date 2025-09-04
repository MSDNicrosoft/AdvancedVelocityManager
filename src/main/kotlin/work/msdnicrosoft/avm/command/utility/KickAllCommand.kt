package work.msdnicrosoft.avm.command.utility

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.util.command.argument.ComponentArgumentType
import work.msdnicrosoft.avm.util.command.argument.ServerArgumentType
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.CommandContext
import work.msdnicrosoft.avm.util.command.context.name
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
        argument("server", ServerArgumentType.registered()) {
            executes {
                val server: String by this
                kickAllPlayers(
                    server,
                    tr(
                        "avm.command.avm.kick.target",
                        Argument.string("executor", context.source.name)
                    )
                )
                Command.SINGLE_SUCCESS
            }
            argument("reason", ComponentArgumentType.miniMessage()) {
                executes {
                    val server: String by this
                    val reason: Component by this
                    kickAllPlayers(server, reason)
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }

    private fun CommandContext.kickAllPlayers(serverName: String, reason: Component) {
        val registeredServer: RegisteredServer = server.getServer(serverName).get()
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
