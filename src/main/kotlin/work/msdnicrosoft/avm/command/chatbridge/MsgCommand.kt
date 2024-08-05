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
import taboolib.common.platform.command.player
import taboolib.common.util.isConsole
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.config.AVMConfig.ChatBridge.Format
import work.msdnicrosoft.avm.util.ComponentUtil.createClickEvent
import work.msdnicrosoft.avm.util.ComponentUtil.createHoverEvent
import work.msdnicrosoft.avm.util.ComponentUtil.serializer
import work.msdnicrosoft.avm.util.DateTimeUtil
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@Suppress("unused")
@PlatformSide(Platform.VELOCITY)
@CommandHeader(name = "msg", aliases = ["tell", "w"], permissionDefault = PermissionDefault.NOT_OP)
object MsgCommand {

    val config
        get() = AVM.config.chatBridge

    @CommandBody
    val main = mainCommand {
        player("targets") {
            suggestion<ProxyCommandSender>(uncheck = false) { sender, _ ->
                if (config.takeOverPrivateChat) {
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
                    if (!sender.isConsole()) {
                        AVM.plugin.server.getPlayer(sender.name).get().sendMessage(
                            buildMessage(config.privateChatFormat.sender, sender, player, context["message"])
                        )
                    }
                    player.sendMessage(
                        buildMessage(config.privateChatFormat.receiver, sender, player, context["message"])
                    )
                }
            }
        }
    }

    private fun buildMessage(formats: List<Format>, sender: ProxyCommandSender, player: Player, message: String) =
        Component.join(
            JoinConfiguration.noSeparators(),
            formats.map { format ->
                format.text.deserialize(sender.name, player.username, message)
                    .hoverEvent(createHoverEvent(format) { deserialize(sender.name, player.username, message) })
                    .clickEvent(createClickEvent(format) { replacePlaceHolders(sender.name, player.username) })
            }
        )

    private fun String.deserialize(from: String, to: String, message: String) = serializer.buildComponent(this)
        .replace("%player_name_from%", from)
        .replace("%player_name_to%", to)
        .replace("%player_message%", message.let { if (config.allowFormatCode) serializer.parse(it) else it })
        .replace("%player_message_sent_time%", DateTimeUtil.getDateTime())
        .build()

    private fun String.replacePlaceHolders(from: String, to: String) = this
        .replace("%player_name_from%", from)
        .replace("%player_name_to%", to)
        .replace("%player_message_sent_time%", DateTimeUtil.getDateTime())
}
