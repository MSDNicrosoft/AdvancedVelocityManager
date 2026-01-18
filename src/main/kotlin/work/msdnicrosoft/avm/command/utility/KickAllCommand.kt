package work.msdnicrosoft.avm.command.utility

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.Component
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.CommandContext
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.command.data.component.MiniMessage
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.component.builder.style.styled
import work.msdnicrosoft.avm.util.server.task

object KickAllCommand {
    val command = literalCommand("kickall") {
        requires { hasPermission("avm.command.kickall") }
        executes {
            server.allPlayers
                .filterNot { it.hasPermission("avm.kickall.bypass") }
                .forEach {
                    it.disconnect(
                        tr("avm.command.avm.kick.target") {
                            args { string("executor", context.source.name) }
                        }
                    )
                }
            Command.SINGLE_SUCCESS
        }
        wordArgument("server") {
            suggests { builder ->
                server.allServers.forEach { builder.suggest(it.serverInfo.name) }
                builder.buildFuture()
            }
            executes {
                val server: RegisteredServer by this
                kickAllPlayers(
                    server,
                    tr("avm.command.avm.kick.target") {
                        args { string("executor", context.source.name) }
                    }
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
            val playersToKick: List<Player> = allPlayers.filterNot { it.hasPermission("avm.kickall.bypass") }

            playersToKick.forEach { it.disconnect(reason) }

            sendTranslatable("avm.command.avm.kickall.executor.text") {
                args {
                    numeric("player_total", playersToKick.size)
                    component(
                        "bypass",
                        tr("avm.command.avm.kickall.executor.bypass.text") {
                            args { numeric("player_bypass", allPlayers.size - playersToKick.size) }
                        } styled { hoverText { tr("avm.command.avm.kickall.executor.bypass.hover") } }
                    )
                }
            }
        }
    }
}
