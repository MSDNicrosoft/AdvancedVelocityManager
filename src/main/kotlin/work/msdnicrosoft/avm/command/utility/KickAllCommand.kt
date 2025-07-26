package work.msdnicrosoft.avm.command.utility

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.translation.Argument
import taboolib.common.platform.function.submitAsync
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.util.ProxyServerUtil.getRegisteredServer
import work.msdnicrosoft.avm.util.command.*
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.tr
import kotlin.jvm.optionals.getOrElse

object KickAllCommand {

    val command: LiteralArgumentBuilder<CommandSource> = literal("kick")
        .requires { source -> source.hasPermission("avm.command.kick") }
        .executes { context ->
            plugin.server.allPlayers.filter { !it.hasPermission("avm.kickall.bypass") }
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
        .then(
            wordArgument("server")
                .suggests { context, builder ->
                    plugin.server.allServers.map { it.serverInfo.name }.forEach(builder::suggest)
                    builder.buildFuture()
                }
                .executes { context ->
                    context.source.kickAllPlayers(
                        context.getString("server"),
                        tr(
                            "avm.command.avm.kick.target",
                            Argument.string("executor", context.source.name)
                        )
                    )
                    Command.SINGLE_SUCCESS
                }
                .then(
                    wordArgument("reason")
                        .executes { context ->
                            context.source.kickAllPlayers(
                                context.getString("server"),
                                miniMessage.deserialize(context.getString("reason"))
                            )
                            Command.SINGLE_SUCCESS
                        }
                )
        )

    private fun CommandSource.kickAllPlayers(serverName: String, reason: Component) {
        val server = getRegisteredServer(serverName).getOrElse {
            sendTranslatable("avm.general.not.exist.server", Argument.string("server", serverName))
            return
        }

        if (server.playersConnected.isEmpty()) {
            sendTranslatable("avm.general.empty.server")
            return
        }

        val (bypassed, toKick) = server.playersConnected.partition { it.hasPermission("avm.kickall.bypass") }

        submitAsync(now = true) {
            toKick.forEach { player ->
                player.disconnect(reason)
            }
        }

        sendTranslatable(
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
