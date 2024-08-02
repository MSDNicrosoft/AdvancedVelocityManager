package work.msdnicrosoft.avm.command.chatbridge

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.player
import taboolib.common.util.isConsole
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.util.ComponentUtil.createClickEvent
import work.msdnicrosoft.avm.util.ComponentUtil.createHoverEvent
import work.msdnicrosoft.avm.util.ComponentUtil.serializer
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@Suppress("unused")
@PlatformSide(Platform.VELOCITY)
@CommandHeader(name = "msg", aliases = ["tell", "w"], permissionDefault = PermissionDefault.NOT_OP)
object MsgCommand {

    @CommandBody
    val main = mainCommand {
        player("targets") {
            suggestion<ProxyCommandSender>(uncheck = false) { sender, _ ->
                if (AVM.config.chatBridge.takeOverPrivateChat) {
                    AVM.plugin.server.allPlayers.map { it.username }
                } else {
                    AVM.plugin.server.getPlayer(sender.name).getOrNull()
                        ?.currentServer?.getOrNull()
                        ?.server?.playersConnected?.map { it.username }
                }
            }
            dynamic("message") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    val targets = context.player("targets")
                    val player = AVM.plugin.server.getPlayer(targets.name).getOrElse {
                        sender.sendLang("player-not-found", targets.name)
                        return@execute
                    }
                    val message = Component.join(
                        JoinConfiguration.noSeparators(),
                        AVM.config.chatBridge.privateChatFormat.receiver.map { format ->
                            format.text.deserialize(sender.name, player.username, context["message"])
                                .hoverEvent(
                                    createHoverEvent(format) {
                                        deserialize(
                                            sender.name,
                                            player.username,
                                            context["message"]
                                        )
                                    }
                                )
                                .clickEvent(
                                    createClickEvent(format) {
                                        replacePlaceHolders(
                                            sender.name,
                                            player.username
                                        )
                                    }
                                )
                        }
                    )
                    if (!sender.isConsole()) {
                        AVM.plugin.server.getPlayer(sender.name).get().sendMessage(message)
                    }
                    player.sendMessage(message)
                }
            }
        }
    }

    private fun String.deserialize(from: String, to: String, message: String) = serializer.buildComponent(this)
        .replace("%player_name_from%", from)
        .replace("%player_name_to%", to)
        .replace(
            "%player_message%",
            message.let { if (AVM.config.chatBridge.allowFormatCode) serializer.parse(it) else it }
        )
        .replace(
            "%player_message_sent_time%",
            LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        .build()

    private fun String.replacePlaceHolders(from: String, to: String) = this
        .replace("%player_name_from%", from)
        .replace("%player_name_to%", to)
        .replace(
            "%player_message_sent_time%",
            LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
}
