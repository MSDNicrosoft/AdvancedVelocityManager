package work.msdnicrosoft.avm.command.utility

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import taboolib.common.platform.function.submitAsync
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.util.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.command.*
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.tr
import kotlin.jvm.optionals.getOrElse

object KickCommand {

    val command: LiteralArgumentBuilder<CommandSource> = literal("kick")
        .requires { source -> source.hasPermission("avm.command.kick") }
        .then(
            wordArgument("player")
                .suggests { context, builder ->
                    plugin.server.allPlayers.map { it.username }.forEach(builder::suggest)
                    builder.buildFuture()
                }.executes { context ->
                    context.source.kickPlayer(
                        context.getString("player"),
                        tr("avm.command.avm.kick.target", Argument.string("executor", context.source.name))
                    )

                    Command.SINGLE_SUCCESS
                }.then(
                    wordArgument("reason")
                        .executes { context ->
                            context.source.kickPlayer(
                                context.getString("player"),
                                miniMessage.deserialize(context.getString("reason"))
                            )

                            Command.SINGLE_SUCCESS
                        }
                )
        )

    private fun CommandSource.kickPlayer(player: String, reason: Component) {
        val playerToKick = getPlayer(player).getOrElse {
            sendTranslatable("avm.general.not.exist.player", Argument.string("player", player))
            return
        }
        submitAsync(now = true) { playerToKick.disconnect(reason) }
    }
}
