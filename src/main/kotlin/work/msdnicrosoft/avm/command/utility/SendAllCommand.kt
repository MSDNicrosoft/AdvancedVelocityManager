package work.msdnicrosoft.avm.command.utility

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.command.argument.ComponentArgumentType
import work.msdnicrosoft.avm.util.command.argument.ServerArgumentType
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.CommandContext
import work.msdnicrosoft.avm.util.command.context.name
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
                        Argument.string("server", getServerNickname(server))
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
        val registeredServer = server.getServer(serverName).get()
        val serverNickname = getServerNickname(serverName)

        if (registeredServer.playersConnected.isEmpty()) {
            this.sendTranslatable("avm.general.empty.server")
            return
        }

        val (bypassed, toSend) = server.allPlayers
            .filter { it.currentServer.get().serverInfo.name != serverName }
            .partition { it.hasPermission("avm.sendall.bypass") }

        task {
            toSend.forEach { player ->
                player.sendToServer(registeredServer).thenAcceptAsync { success ->
                    if (success) player.sendMessage(reason)
                }
            }

            this.sendTranslatable(
                "avm.command.avm.sendall.executor.text",
                Argument.numeric("player_total", toSend.size),
                Argument.string("server", serverNickname),
                Argument.component(
                    "bypass",
                    tr(
                        "avm.command.avm.sendall.executor.bypass.text",
                        Argument.numeric("player_bypass", bypassed.size)
                    ).hoverEvent(HoverEvent.showText(tr("avm.command.avm.sendall.executor.bypass.hover")))
                )
            )
        }
    }
}
