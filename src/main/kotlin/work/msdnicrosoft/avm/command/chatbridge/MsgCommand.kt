package work.msdnicrosoft.avm.command.chatbridge

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.DateTimeUtil.getDateTime
import work.msdnicrosoft.avm.util.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.command.*
import work.msdnicrosoft.avm.util.component.ComponentUtil.createClickEvent
import work.msdnicrosoft.avm.util.component.ComponentUtil.createHoverEvent
import work.msdnicrosoft.avm.util.component.ComponentUtil.styleOnlyMiniMessage
import work.msdnicrosoft.avm.util.component.Format
import kotlin.jvm.optionals.getOrElse

object MsgCommand {

    private inline val config
        get() = ConfigManager.config.chatBridge

    val aliases = listOf("msg", "tell", "w")

    fun init() {
        command.register("tell", "w")
    }

    fun disable() {
        command.unregister()
    }

    val command: LiteralCommandNode<CommandSource> = literal("msg").then(
        wordArgument("targets")
            .suggests { context, builder ->
                val source = context.source

                val suggestions = if (config.takeOverPrivateChat || source.isConsole) {
                    plugin.server.allPlayers.map { it.username }
                } else {
                    source.toPlayer().currentServer.get().server.playersConnected.map { it.username }
                }
                suggestions.forEach(builder::suggest)
                builder.buildFuture()
            }.then(
                wordArgument("message")
                    .suggests { context, builder ->
                        builder.buildFuture()
                    }
                    .executes { context ->
                        val source = context.source

                        val targets = context.getString("targets")
                        val player = getPlayer(targets).getOrElse {
                            source.sendTranslatable(
                                "avm.general.not.exist.player",
                                Argument.string("player", targets)
                            )
                            return@executes Command.SINGLE_SUCCESS
                        }

                        val message = context.getString("message")

                        if (!source.isConsole) {
                            source.sendMessage(config.privateChatFormat.sender.buildMessage(source, player, message))
                        }
                        player.sendMessage(config.privateChatFormat.receiver.buildMessage(source, player, message))

                        Command.SINGLE_SUCCESS
                    }
            )
    ).build()

    private fun List<Format>.buildMessage(sender: CommandSource, player: Player, message: String): Component {
        val time = getDateTime()
        return Component.join(
            JoinConfiguration.noSeparators(),
            map { format ->
                format.text.deserialize(sender.name, player.username, message, time)
                    .hoverEvent(createHoverEvent(format) { deserialize(sender.name, player.username, message, time) })
                    .clickEvent(createClickEvent(format) { replacePlaceHolders(sender.name, player.username, time) })
            }
        )
    }

    private fun String.deserialize(from: String, to: String, message: String, dateTime: String): Component =
        styleOnlyMiniMessage.deserialize(
            this,
            Placeholder.unparsed("player_name_from", from),
            Placeholder.unparsed("player_name_to", to),
            if (config.allowFormatCode) {
                Placeholder.parsed("player_message", message)
            } else {
                Placeholder.unparsed("player_message", message)
            },
            Placeholder.unparsed("player_message_sent_time", dateTime),
        )

    private fun String.replacePlaceHolders(from: String, to: String, dateTime: String): String = this
        .replace("<player_name_from>", from)
        .replace("<player_name_to>", to)
        .replace("<player_message_sent_time>", dateTime)
}
