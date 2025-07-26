package work.msdnicrosoft.avm.command.utility

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.translation.Argument
import taboolib.common.platform.function.submitAsync
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.ProxyServerUtil.getRegisteredServer
import work.msdnicrosoft.avm.util.ProxyServerUtil.sendPlayer
import work.msdnicrosoft.avm.util.command.*
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.tr
import kotlin.jvm.optionals.getOrElse

object SendAllCommand {

    val command: LiteralArgumentBuilder<CommandSource> = literal("sendall")
        .requires { source -> source.hasPermission("avm.command.sendall") }
        .then(
            wordArgument("server")
                .suggests { context, builder ->
                    plugin.server.allServers.map { it.serverInfo.name }.forEach(builder::suggest)
                    builder.buildFuture()
                }
                .executes { context ->
                    val serverName = context.getString("server")
                    context.source.sendAllPlayers(
                        serverName,
                        tr(
                            "avm.command.avm.send.target",
                            Argument.string("executor", context.source.name),
                            Argument.string("server", getServerNickname(serverName))
                        )
                    )
                    Command.SINGLE_SUCCESS
                }.then(
                    wordArgument("reason")
                        .executes { context ->
                            context.source.sendAllPlayers(
                                context.getString("reason"),
                                miniMessage.deserialize(context.getString("reason"))
                            )
                            Command.SINGLE_SUCCESS
                        }
                )
        )

    private fun CommandSource.sendAllPlayers(serverName: String, reason: Component) {
        val server = getRegisteredServer(serverName).getOrElse {
            sendTranslatable("avm.general.not.exist.server", Argument.string("server", serverName))
            return
        }
        val serverNickname = getServerNickname(serverName)

        if (server.playersConnected.isEmpty()) {
            sendTranslatable("avm.general.empty.server")
            return
        }

        val (bypassed, toSend) = plugin.server.allPlayers
            .filter { it.currentServer.get().serverInfo.name != serverName }
            .partition { it.hasPermission("avm.sendall.bypass") }

        submitAsync(now = true) {
            toSend.forEach { player ->
                sendPlayer(server, player).thenAcceptAsync { success ->
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
