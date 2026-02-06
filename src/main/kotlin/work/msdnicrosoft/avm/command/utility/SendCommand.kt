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
import work.msdnicrosoft.avm.util.server.nickname
import work.msdnicrosoft.avm.util.server.sendToServer

object SendCommand {
    val command = literalCommand("send") {
        requires { hasPermission("avm.command.send") }
        wordArgument("player") {
            suggests { builder ->
                server.allPlayers.forEach { builder.suggest(it.username) }
                builder.buildFuture()
            }
            wordArgument("server") {
                suggests { builder ->
                    server.allServers.forEach { builder.suggest(it.serverInfo.name) }
                    builder.buildFuture()
                }
                executes {
                    val server: RegisteredServer by this
                    val player: Player by this
                    sendPlayer(
                        player,
                        server,
                        tr("avm.command.avm.send.target") {
                            args {
                                string("executor", context.source.name)
                                component("server", server.serverInfo.nickname)
                            }
                        }
                    )
                    Command.SINGLE_SUCCESS
                }
                stringArgument("reason") {
                    executes {
                        val server: RegisteredServer by this
                        val player: Player by this
                        val reason: MiniMessage by this
                        sendPlayer(player, server, reason.component)
                        Command.SINGLE_SUCCESS
                    }
                }
            }
        }
    }

    private fun CommandContext.sendPlayer(player: Player, registeredServer: RegisteredServer, reason: Component) {
        val serverNickname: Component = registeredServer.serverInfo.nickname

        player.sendToServer(registeredServer).thenAcceptAsync { success: Boolean ->
            if (success) {
                this.sendTranslatable("avm.command.avm.send.executor.success") {
                    args {
                        string("player", player.name)
                        component("server", serverNickname)
                    }
                }
                player.sendMessage(reason)
            } else {
                this.sendTranslatable("avm.command.avm.send.executor.failed") {
                    args {
                        string("player", player.name)
                        component("server", serverNickname)
                    }
                }
            }
        }
    }
}
