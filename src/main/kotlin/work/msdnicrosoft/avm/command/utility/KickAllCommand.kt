package work.msdnicrosoft.avm.command.utility

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.getRegisteredServer
import work.msdnicrosoft.avm.util.server.task
import kotlin.jvm.optionals.getOrElse

object KickAllCommand {

    val command = literalCommand("kickall") {
        requires { hasPermission("avm.command.kickall") }
        executes {
            server.allPlayers.filter { !it.hasPermission("avm.kickall.bypass") }
                .forEach {
                    it.disconnect(
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
                server.allServers.forEach { builder.suggest(it.serverInfo.name) }
                builder.buildFuture()
            }
            executes {
                val server: String by this
                context.source.kickAllPlayers(
                    server,
                    tr(
                        "avm.command.avm.kick.target",
                        Argument.string("executor", context.source.name)
                    )
                )
                Command.SINGLE_SUCCESS
            }
            wordArgument("reason") {
                executes {
                    val server: String by this
                    val reason: String by this
                    context.source.kickAllPlayers(server, miniMessage.deserialize(reason))
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }

    private fun CommandSource.kickAllPlayers(serverName: String, reason: Component) {
        val server = getRegisteredServer(serverName).getOrElse {
            this.sendTranslatable("avm.general.not.exist.server", Argument.string("server", serverName))
            return
        }

        if (server.playersConnected.isEmpty()) {
            this.sendTranslatable("avm.general.empty.server")
            return
        }

        val (bypassed, toKick) = server.playersConnected.partition { it.hasPermission("avm.kickall.bypass") }

        task {
            toKick.forEach { player ->
                player.disconnect(reason)
            }
            this.sendTranslatable(
                "avm.command.avm.kickall.executor.text",
                Argument.numeric("player_total", toKick.size),
                Argument.component(
                    "bypass",
                    tr(
                        "avm.command.avm.kickall.executor.bypass.text",
                        Argument.numeric("player_bypass", bypassed.size)
                    ).hoverEvent(HoverEvent.showText(tr("avm.command.avm.kickall.executor.bypass.hover")))
                )

            )
        }
    }
}
