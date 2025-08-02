package work.msdnicrosoft.avm.command.utility

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.util.command.get
import work.msdnicrosoft.avm.util.command.literal
import work.msdnicrosoft.avm.util.command.name
import work.msdnicrosoft.avm.util.command.wordArgument
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.server.task
import kotlin.jvm.optionals.getOrElse

object KickCommand {

    val command: LiteralArgumentBuilder<CommandSource> = literal("kick")
        .requires { source -> source.hasPermission("avm.command.kick") }
        .then(
            wordArgument("player")
                .suggests { context, builder ->
                    server.allPlayers.forEach { builder.suggest(it.username) }
                    builder.buildFuture()
                }
                .executes { context ->
                    context.source.kickPlayer(
                        context.get<String>("player"),
                        tr("avm.command.avm.kick.target", Argument.string("executor", context.source.name))
                    )

                    Command.SINGLE_SUCCESS
                }
                .then(
                    wordArgument("reason")
                        .executes { context ->
                            context.source.kickPlayer(
                                context.get<String>("player"),
                                miniMessage.deserialize(context.get<String>("reason"))
                            )

                            Command.SINGLE_SUCCESS
                        }
                )
        )

    private fun CommandSource.kickPlayer(player: String, reason: Component) {
        val playerToKick = getPlayer(player).getOrElse {
            this.sendTranslatable("avm.general.not.exist.player", Argument.string("player", player))
            return
        }
        task { playerToKick.disconnect(reason) }
    }
}
