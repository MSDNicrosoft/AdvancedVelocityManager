package work.msdnicrosoft.avm.command.utility

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager.config
import work.msdnicrosoft.avm.util.command.argument.ComponentArgumentType
import work.msdnicrosoft.avm.util.command.argument.ServerArgumentType
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.CommandContext
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.component.hoverText
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.sendToServer
import work.msdnicrosoft.avm.util.server.task

object SendAllCommand {
    val command = literalCommand("sendall") {
        requires { hasPermission("avm.command.sendall") }
        argument("server", ServerArgumentType.registered()) {
            executes {
                val server: String by this
                sendAllPlayers(
                    server,
                    tr(
                        "avm.command.avm.send.target",
                        Argument.string("executor", context.source.name),
                        Argument.string("server", config.getServerNickName(server))
                    )
                )
                Command.SINGLE_SUCCESS
            }
            argument("reason", ComponentArgumentType.miniMessage()) {
                executes {
                    val server: String by this
                    val reason: Component by this
                    sendAllPlayers(server, reason)
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }

    private fun CommandContext.sendAllPlayers(serverName: String, reason: Component) {
        val registeredServer: RegisteredServer = server.getServer(serverName).get()

        if (registeredServer.playersConnected.isEmpty()) {
            this.sendTranslatable("avm.general.empty.server")
            return
        }

        task {
            val allPlayers: List<Player> = server.allPlayers.filterNot { player ->
                player.currentServer.get().serverInfo.name == serverName
            }
            val toSend: List<Player> = allPlayers.filterNot { player ->
                player.hasPermission("avm.sendall.bypass")
            }

            toSend.forEach { player ->
                player.sendToServer(registeredServer).thenAcceptAsync { success: Boolean ->
                    if (success) player.sendMessage(reason)
                }
            }

            this.sendTranslatable(
                "avm.command.avm.sendall.executor.text",
                Argument.numeric("player_total", toSend.size),
                Argument.string("server", config.getServerNickName(serverName)),
                Argument.component(
                    "bypass",
                    tr(
                        "avm.command.avm.sendall.executor.bypass.text",
                        Argument.numeric("player_bypass", allPlayers.size - toSend.size)
                    ).hoverText(tr("avm.command.avm.sendall.executor.bypass.hover"))
                )
            )
        }
    }
}
