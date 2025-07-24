package work.msdnicrosoft.avm.command.whitelist

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submitAsync
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.CommandSessionManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@PlatformSide(Platform.VELOCITY)
object ClearCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command = subCommand {
        execute<ProxyCommandSender> { sender, _, argument ->
            val sessionId = CommandSessionManager.generateSessionId(sender.name, System.currentTimeMillis(), argument)

            CommandSessionManager.add(sessionId) {
                if (WhitelistManager.clear()) {
                    sender.sendLang("command-avmwl-clear-success")
                } else {
                    sender.sendLang("command-avmwl-clear-failed")
                }
                if (config.enabled) {
                    submitAsync(now = true) {
                        kickPlayers(config.message, AVM.plugin.server.allPlayers)
                    }
                }
            }
            sender.sendLang("command-avmwl-clear-need-confirm", "/avm confirm $sessionId")
        }
    }
}
