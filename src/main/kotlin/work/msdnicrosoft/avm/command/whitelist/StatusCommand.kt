package work.msdnicrosoft.avm.command.whitelist

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager

@PlatformSide(Platform.VELOCITY)
object StatusCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            val state = if (WhitelistManager.enabled) "on" else "off"
            sender.sendLang("command-avmwl-state", sender.asLangText("general-$state"))
            sender.sendLang("command-avmwl-list-header", WhitelistManager.size)
            sender.sendLang("command-avmwl-status-cache", PlayerCache.readOnly.size, config.cachePlayers.maxSize)
        }
    }
}
