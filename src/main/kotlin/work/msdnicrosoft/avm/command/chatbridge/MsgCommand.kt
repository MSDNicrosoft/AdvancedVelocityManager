package work.msdnicrosoft.avm.command.chatbridge

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.mainCommand
import taboolib.common.util.isConsole
import taboolib.common.util.presentRun
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.DateTimeUtil.getDateTime
import work.msdnicrosoft.avm.util.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.component.ComponentUtil.createClickEvent
import work.msdnicrosoft.avm.util.component.ComponentUtil.createHoverEvent
import work.msdnicrosoft.avm.util.component.ComponentUtil.serializer
import work.msdnicrosoft.avm.util.component.Format
import kotlin.jvm.optionals.getOrElse
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@PlatformSide(Platform.VELOCITY)
@CommandHeader(name = "msg", aliases = ["tell", "w"], permissionDefault = PermissionDefault.NOT_OP)
object MsgCommand {

    private inline val config
        get() = ConfigManager.config.chatBridge

    @CommandBody
    val main = mainCommand {
        dynamic("targets") {
            suggestion<ProxyCommandSender>(uncheck = false) { sender, _ ->
                if (config.takeOverPrivateChat || sender.isConsole()) {
                    AVM.plugin.server.allPlayers.map { it.username }
                } else {
                    getPlayer(sender.name).get()
                        .currentServer.get()
                        .server.playersConnected.map { it.username }
                }
            }
            dynamic("message") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    val targets = context["targets"]
                    val player = getPlayer(targets).getOrElse {
                        sender.sendLang("player-not-found", targets)
                        return@execute
                    }

                    val message = context["message"]

                    if (!sender.isConsole()) {
                        getPlayer(sender.name).presentRun {
                            sendMessage(
                                config.privateChatFormat.sender.buildMessage(sender, player, message)
                            )
                        }
                    }
                    player.sendMessage(
                        config.privateChatFormat.receiver.buildMessage(sender, player, message)
                    )
                }
            }
        }
    }

    private fun List<Format>.buildMessage(sender: ProxyCommandSender, player: Player, message: String): Component {
        val time = getDateTime()
        return Component.join(
            JoinConfiguration.noSeparators(),
            this.map { format ->
                format.text.deserialize(sender.name, player.username, message, time)
                    .hoverEvent(createHoverEvent(format) { deserialize(sender.name, player.username, message, time) })
                    .clickEvent(createClickEvent(format) { replacePlaceHolders(sender.name, player.username, time) })
            }
        )
    }

    private fun String.deserialize(from: String, to: String, message: String, dateTime: String): Component =
        serializer.buildComponent(this)
            .replace("%player_name_from%", from)
            .replace("%player_name_to%", to)
            .replace("%player_message%", message.let { if (config.allowFormatCode) SERIALIZER.parse(it) else it })
            .replace("%player_message_sent_time%", dateTime)
            .build()

    private fun String.replacePlaceHolders(from: String, to: String, dateTime: String): String = this
        .replace("%player_name_from%", from)
        .replace("%player_name_to%", to)
        .replace("%player_message_sent_time%", dateTime)
}
