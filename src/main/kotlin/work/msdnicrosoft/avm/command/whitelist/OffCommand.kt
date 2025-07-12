package work.msdnicrosoft.avm.command.whitelist

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager

@PlatformSide(Platform.VELOCITY)
object OffCommand {
    val command = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            WhitelistManager.enabled = false
            sender.sendLang("command-avmwl-state", sender.asLangText("general-off"))
        }
    }
}
