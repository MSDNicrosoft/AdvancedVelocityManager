package work.msdnicrosoft.avm.command.utility

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.command.get
import work.msdnicrosoft.avm.util.command.literal
import work.msdnicrosoft.avm.util.command.name
import work.msdnicrosoft.avm.util.command.wordArgument
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.getRegisteredServer
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.sendPlayer
import kotlin.jvm.optionals.getOrElse

object SendCommand {

    val command: LiteralArgumentBuilder<CommandSource> = literal("send")
        .requires { source -> source.hasPermission("avm.command.send") }
        .then(
            wordArgument("player")
                .suggests { context, builder ->
                    plugin.server.allPlayers.forEach { builder.suggest(it.username) }
                    builder.buildFuture()
                }
                .then(
                    wordArgument("server")
                        .suggests { context, builder ->
                            plugin.server.allServers.forEach { builder.suggest(it.serverInfo.name) }
                            builder.buildFuture()
                        }
                        .executes { context ->
                            val serverName = context.get<String>("server")
                            val serverNickname = getServerNickname(serverName)
                            context.source.sendPlayer(
                                context.get<String>("player"),
                                serverName,
                                tr(
                                    "avm.command.avm.send.target",
                                    Argument.string("executor", context.source.name),
                                    Argument.string("server", serverNickname)
                                )
                            )
                            Command.SINGLE_SUCCESS
                        }
                        .then(
                            wordArgument("reason")
                                .executes { context ->
                                    context.source.sendPlayer(
                                        context.get<String>("player"),
                                        context.get<String>("server"),
                                        miniMessage.deserialize(context.get<String>("reason"))
                                    )

                                    Command.SINGLE_SUCCESS
                                }
                        )
                )
        )

    private fun CommandSource.sendPlayer(playerName: String, serverName: String, reason: Component) {
        val server = getRegisteredServer(serverName).getOrElse {
            this.sendTranslatable("avm.general.not.exist.server", Argument.string("server", serverName))
            return
        }
        val serverNickname = getServerNickname(serverName)

        val player = getPlayer(playerName).getOrElse {
            this.sendTranslatable("avm.general.not.exist.player", Argument.string("player", serverName))
            return
        }

        sendPlayer(server, player).thenAccept { success ->
            if (success) {
                this.sendTranslatable(
                    "avm.command.avm.send.executor.success",
                    Argument.string("player", playerName),
                    Argument.string("server", serverNickname)
                )
                player.sendMessage(reason)
            } else {
                this.sendTranslatable(
                    "avm.command.avm.send.executor.failed",
                    Argument.string("player", playerName),
                    Argument.string("server", serverNickname)
                )
            }
        }
    }
}
