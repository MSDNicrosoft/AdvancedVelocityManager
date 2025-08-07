package work.msdnicrosoft.avm.command.whitelist

import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.CommandSessionManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.builder.Command
import work.msdnicrosoft.avm.util.command.builder.executes
import work.msdnicrosoft.avm.util.command.builder.literalCommand
import work.msdnicrosoft.avm.util.command.builder.requires
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.util.server.task

object ClearCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command = literalCommand("clear") {
        requires { hasPermission("avm.command.whitelist.clear") }
        executes {
            val sessionId =
                CommandSessionManager.generateSessionId(
                    context.source.name,
                    System.currentTimeMillis(),
                    context.arguments.values.joinToString(" ")
                )

            CommandSessionManager.add(sessionId) {
                if (WhitelistManager.clear()) {
                    context.source.sendTranslatable("avm.command.avmwl.clear.success")
                } else {
                    context.source.sendTranslatable("avm.command.avmwl.clear.failed")
                }
                if (config.enabled) {
                    task { kickPlayers(config.message, server.allPlayers) }
                }
            }
            context.source.sendTranslatable("avm.command.avmwl.clear.need.confirm.1.text")
            context.source.sendMessage(
                tr(
                    "avm.command.avmwl.clear.need.confirm.2.text",
                    Argument.string("command", "/avm confirm $sessionId")
                )
                    .clickEvent(ClickEvent.runCommand("/avm confirm $sessionId"))
                    .hoverEvent(HoverEvent.showText(tr("avm.command.avmwl.clear.need.confirm.2.hover")))
            )

            Command.SINGLE_SUCCESS
        }
    }
}
