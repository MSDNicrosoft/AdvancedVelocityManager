package work.msdnicrosoft.avm.command.chatbridge

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.DateTimeUtil.getDateTime
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.isConsole
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.command.context.toPlayer
import work.msdnicrosoft.avm.util.command.register
import work.msdnicrosoft.avm.util.command.unregister
import work.msdnicrosoft.avm.util.component.ComponentSerializer.STYLE_ONLY_MINI_MESSAGE
import work.msdnicrosoft.avm.util.component.ComponentUtil.createClickEvent
import work.msdnicrosoft.avm.util.component.Format
import work.msdnicrosoft.avm.util.component.hoverText

object MsgCommand {
    private inline val chatFormat get() = ConfigManager.config.chatBridge.privateChatFormat
    private inline val shouldTakeOverPrivateChat: Boolean get() = ConfigManager.config.chatBridge.takeOverPrivateChat
    private inline val allowFormatCode: Boolean get() = ConfigManager.config.chatBridge.allowFormatCode

    val aliases: List<String> = listOf("msg", "tell", "w")

    val command = literalCommand("msg") {
        stringArgument("targets") {
            suggests { builder ->
                val players: List<String> = if (shouldTakeOverPrivateChat || context.source.isConsole) {
                    server.allPlayers.map { it.username }
                } else {
                    context.source.toPlayer().currentServer.get().server.playersConnected.map { it.username }
                }
                players.forEach(builder::suggest)
                builder.buildFuture()
            }
            greedyStringArgument("message") {
                executes {
                    val targets: Player by this
                    val message: String by this

                    if (!context.source.isConsole) {
                        sendMessage(chatFormat.sender.buildMessage(context.source, targets, message))
                    }
                    targets.sendMessage(chatFormat.receiver.buildMessage(context.source, targets, message))
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }.build()

    fun init() {
        this.command.register("tell", "w")
    }

    fun disable() {
        this.command.unregister()
    }

    private fun List<Format>.buildMessage(source: CommandSource, player: Player, message: String): Component {
        val time: String = getDateTime()
        return Component.join(
            JoinConfiguration.noSeparators(),
            this.map { format ->
                format.text.deserialize(source.name, player.username, message, time)
                    .hoverText(
                        format.hover?.joinToString("\n")
                            ?.deserialize(source.name, player.username, message, time)
                    ).clickEvent(createClickEvent(format) { replacePlaceHolders(source.name, player.username, time) })
            }
        )
    }

    private fun String.deserialize(from: String, to: String, message: String, dateTime: String): Component =
        STYLE_ONLY_MINI_MESSAGE.deserialize(
            this,
            Placeholder.unparsed("player_name_from", from),
            Placeholder.unparsed("player_name_to", to),
            if (allowFormatCode) {
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
