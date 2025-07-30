package work.msdnicrosoft.avm.command.utility

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.command.*
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.getRegisteredServer
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.sendPlayer
import work.msdnicrosoft.avm.util.server.task
import kotlin.jvm.optionals.getOrElse

object SendAllCommand {

    val command: LiteralArgumentBuilder<CommandSource> = literal("sendall")
        .requires { source -> source.hasPermission("avm.command.sendall") }
        .then(
            wordArgument("server")
                .suggests { context, builder ->
                    server.allServers.forEach { builder.suggest(it.serverInfo.name) }
                    builder.buildFuture()
                }
                .executes { context ->
                    val serverName = context.get<String>("server")
                    context.source.sendAllPlayers(
                        serverName,
                        tr(
                            "avm.command.avm.send.target",
                            Argument.string("executor", context.source.name),
                            Argument.string("server", getServerNickname(serverName))
                        )
                    )
                    Command.SINGLE_SUCCESS
                }
                .then(
                    wordArgument("reason")
                        .executes { context ->
                            context.source.sendAllPlayers(
                                context.get<String>("reason"),
                                miniMessage.deserialize(context.get<String>("reason"))
                            )
                            Command.SINGLE_SUCCESS
                        }
                )
        )

    private fun CommandSource.sendAllPlayers(serverName: String, reason: Component) {
        val registeredServer = getRegisteredServer(serverName).getOrElse {
            sendTranslatable("avm.general.not.exist.server", Argument.string("server", serverName))
            return
        }
        val serverNickname = getServerNickname(serverName)

        if (registeredServer.playersConnected.isEmpty()) {
            sendTranslatable("avm.general.empty.server")
            return
        }

        val (bypassed, toSend) = server.allPlayers
            .filter { it.currentServer.get().serverInfo.name != serverName }
            .partition { it.hasPermission("avm.sendall.bypass") }

        task {
            toSend.forEach { player ->
                sendPlayer(registeredServer, player).thenAcceptAsync { success ->
                    if (success) {
                        player.sendMessage(reason)
                    }
                }
            }

            sendTranslatable(
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
