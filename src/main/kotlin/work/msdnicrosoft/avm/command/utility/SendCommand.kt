package work.msdnicrosoft.avm.command.utility

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.command.brigadier.*
import work.msdnicrosoft.avm.util.command.name
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.getRegisteredServer
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.sendPlayer
import kotlin.jvm.optionals.getOrElse

object SendCommand {

    val command = literalCommand("send") {
        requires { hasPermission("avm.command.send") }
        wordArgument("player") {
            suggests { builder ->
                plugin.server.allPlayers.forEach { builder.suggest(it.username) }
                builder.buildFuture()
            }
            wordArgument("server") {
                suggests { builder ->
                    plugin.server.allServers.forEach { builder.suggest(it.serverInfo.name) }
                    builder.buildFuture()
                }
                executes {
                    val server: String by this
                    val player: String by this
                    val serverNickname = getServerNickname(server)
                    context.source.sendPlayer(
                        player,
                        server,
                        tr(
                            "avm.command.avm.send.target",
                            Argument.string("executor", context.source.name),
                            Argument.string("server", serverNickname)
                        )
                    )
                    Command.SINGLE_SUCCESS
                }
                wordArgument("reason") {
                    executes {
                        val server: String by this
                        val player: String by this
                        val reason: String by this
                        context.source.sendPlayer(player, server, miniMessage.deserialize(reason))
                        Command.SINGLE_SUCCESS
                    }
                }
            }
        }
    }

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
