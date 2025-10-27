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
import work.msdnicrosoft.avm.util.server.nickname
import work.msdnicrosoft.avm.util.server.sendToServer
import work.msdnicrosoft.avm.util.server.task

object SendAllCommand {
    val command = literalCommand("sendall") {
        requires { hasPermission("avm.command.sendall") }
        wordArgument("server") {
            suggests { builder ->
                server.allServers.forEach { builder.suggest(it.serverInfo.name) }
                builder.buildFuture()
            }
            executes {
                val server: RegisteredServer by this
                sendAllPlayers(
                    server,
                    tr("avm.command.avm.send.target") {
                        args {
                            string("executor", context.source.name)
                            string("server", server.serverInfo.nickname)
                        }
                    }
                )
                Command.SINGLE_SUCCESS
            }
            stringArgument("reason") {
                executes {
                    val server: RegisteredServer by this
                    val reason: MiniMessage by this
                    sendAllPlayers(server, reason.component)
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }

    private fun CommandContext.sendAllPlayers(registeredServer: RegisteredServer, reason: Component) {
        if (registeredServer.playersConnected.isEmpty()) {
            this.sendTranslatable("avm.general.empty.server")
            return
        }

        task {
            val allPlayers: List<Player> = server.allPlayers.filterNot { player ->
                player.currentServer.get() == registeredServer
            }
            val toSend: List<Player> = allPlayers.filterNot { player ->
                player.hasPermission("avm.sendall.bypass")
            }

            toSend.forEach { player ->
                player.sendToServer(registeredServer).thenAcceptAsync { success: Boolean ->
                    if (success) player.sendMessage(reason)
                }
            }

            this.sendTranslatable("avm.command.avm.sendall.executor.text") {
                args {
                    numeric("player_total", toSend.size)
                    string("server", registeredServer.serverInfo.nickname)
                    component(
                        "bypass",
                        tr("avm.command.avm.sendall.executor.bypass.text") {
                            args { numeric("player_bypass", allPlayers.size - toSend.size) }
                        } styled { hoverText { tr("avm.command.avm.sendall.executor.bypass.hover") } }
                    )
                }
            }
        }
    }
}
