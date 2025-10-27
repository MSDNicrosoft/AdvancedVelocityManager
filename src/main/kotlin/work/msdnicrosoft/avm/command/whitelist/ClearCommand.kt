package work.msdnicrosoft.avm.command.whitelist

import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.command.session.CommandSessionManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.builder.Command
import work.msdnicrosoft.avm.util.command.builder.executes
import work.msdnicrosoft.avm.util.command.builder.literalCommand
import work.msdnicrosoft.avm.util.command.builder.requires
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.component.builder.style.styled
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.util.server.task

object ClearCommand {
    private inline val config get() = ConfigManager.config.whitelist

    val command = literalCommand("clear") {
        requires { hasPermission("avm.command.whitelist.clear") }
        executes {
            val sessionId: String = CommandSessionManager.generateSessionId(
                context.source.name,
                System.currentTimeMillis(),
                context.arguments.values.joinToString(" ")
            )

            CommandSessionManager.add(sessionId) {
                if (WhitelistManager.clear()) {
                    sendTranslatable("avm.command.avmwl.clear.success")
                } else {
                    sendTranslatable("avm.command.avmwl.clear.failed")
                }
                if (config.enabled) {
                    task { kickPlayers(config.message, server.allPlayers) }
                }
            }
            sendTranslatable("avm.command.avmwl.clear.need_confirm.1.text")
            sendMessage(
                tr("avm.command.avmwl.clear.need_confirm.2.text") {
                    args { string("command", "/avm confirm $sessionId") }
                } styled {
                    hoverText { tr("avm.command.avmwl.clear.need_confirm.2.hover") }
                    click { runCommand("/avm confirm $sessionId") }
                }
            )
            Command.SINGLE_SUCCESS
        }
    }
}
