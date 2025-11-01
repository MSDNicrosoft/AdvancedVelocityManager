package work.msdnicrosoft.avm.command.chatbridge

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.DateTimeUtil.getDateTime
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.isConsole
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.command.context.toPlayer
import work.msdnicrosoft.avm.util.command.register
import work.msdnicrosoft.avm.util.command.unregister
import work.msdnicrosoft.avm.util.component.ComponentSerializer
import work.msdnicrosoft.avm.util.component.Format
import work.msdnicrosoft.avm.util.component.builder.minimessage.miniMessage
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.placeholders
import work.msdnicrosoft.avm.util.component.builder.style.styled

object MsgCommand {
    private inline val chatFormat get() = ConfigManager.config.chatBridge.privateChatFormat
    private inline val shouldTakeOverPrivateChat: Boolean get() = ConfigManager.config.chatBridge.takeOverPrivateChat
    private inline val allowFormatCode: Boolean get() = ConfigManager.config.chatBridge.allowFormatCode

    val aliases: List<String> = listOf("msg", "tell", "w")

    val command = literalCommand("msg") {
        stringArgument("targets") {
            suggests { builder ->
                val players: Collection<Player> = if (shouldTakeOverPrivateChat || context.source.isConsole) {
                    server.allPlayers
                } else {
                    context.source.toPlayer().currentServer.get().server.playersConnected
                }
                players.forEach { builder.suggest(it.username) }
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
        val tagResolvers: List<TagResolver> = placeholders {
            unparsed("player_name_from", source.name)
            unparsed("player_name_to", player.username)
            if (allowFormatCode) {
                parsed("player_message", message)
            } else {
                unparsed("player_message", message)
            }
            unparsed("player_message_sent_time", time)
        }
        return Component.join(
            JoinConfiguration.noSeparators(),
            this.map { format ->
                miniMessage(format.text, provider = ComponentSerializer.STYLE_ONLY_MINI_MESSAGE) {
                    placeholders { tagResolvers(tagResolvers) }
                } styled {
                    hoverText {
                        miniMessage(format.hover?.joinToString("\n").orEmpty()) {
                            placeholders { tagResolvers(tagResolvers) }
                        }
                    }
                    click(format.applyReplace { replacePlaceHolders(source.name, player.username, time) })
                }
            }
        )
    }

    private fun String.replacePlaceHolders(from: String, to: String, time: String): String = this
        .replace("<player_name_from>", from)
        .replace("<player_name_to>", to)
        .replace("<player_message_sent_time>", time)
}
