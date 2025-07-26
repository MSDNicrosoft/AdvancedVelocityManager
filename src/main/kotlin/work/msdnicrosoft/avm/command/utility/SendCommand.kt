package work.msdnicrosoft.avm.command.utility

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.ProxyServerUtil.getRegisteredServer
import work.msdnicrosoft.avm.util.ProxyServerUtil.sendPlayer
import work.msdnicrosoft.avm.util.command.*
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.tr
import kotlin.jvm.optionals.getOrElse

object SendCommand {

    val command: LiteralArgumentBuilder<CommandSource> = literal("send")
        .requires { source -> source.hasPermission("avm.command.send") }
        .then(
            wordArgument("player")
                .suggests { context, builder ->
                    plugin.server.allPlayers.map { it.username }.forEach(builder::suggest)
                    builder.buildFuture()
                }.then(
                    wordArgument("server")
                        .suggests { context, builder ->
                            plugin.server.allServers.map { it.serverInfo.name }.forEach(builder::suggest)
                            builder.buildFuture()
                        }.executes { context ->
                            val serverName = context.getString("server")
                            val serverNickname = getServerNickname(serverName)
                            context.source.sendPlayer(
                                context.getString("player"),
                                serverName,
                                tr(
                                    "avm.command.avm.send.target",
                                    Argument.string("executor", context.source.name),
                                    Argument.string("server", serverNickname)
                                )
                            )
                            Command.SINGLE_SUCCESS
                        }.then(
                            wordArgument("reason").executes { context ->
                                context.source.sendPlayer(
                                    context.getString("player"),
                                    context.getString("server"),
                                    miniMessage.deserialize(context.getString("reason"))
                                )

                                Command.SINGLE_SUCCESS
                            }
                        )
                )
        )

    private fun CommandSource.sendPlayer(playerName: String, serverName: String, reason: Component) {
        val server = getRegisteredServer(serverName).getOrElse {
            sendTranslatable("avm.general.not.exist.server", Argument.string("server", serverName))
            return
        }
        val serverNickname = getServerNickname(serverName)

        val player = getPlayer(playerName).getOrElse {
            sendTranslatable("avm.general.not.exist.player", Argument.string("player", serverName))
            return
        }

        sendPlayer(server, player).thenAccept { success ->
            if (success) {
                sendTranslatable(
                    "avm.command.avm.send.executor.success",
                    Argument.string("player", playerName),
                    Argument.string("server", serverNickname)
                )
                player.sendMessage(reason)
            } else {
                sendTranslatable(
                    "avm.command.avm.send.executor.failed",
                    Argument.string("player", playerName),
                    Argument.string("server", serverNickname)
                )
            }
        }
    }
}
