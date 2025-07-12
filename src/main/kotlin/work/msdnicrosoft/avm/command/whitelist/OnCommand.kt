package work.msdnicrosoft.avm.command.whitelist

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submitAsync
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@PlatformSide(Platform.VELOCITY)
object OnCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            WhitelistManager.enabled = true

            sender.sendLang("command-avmwl-state", sender.asLangText("general-on"))

            submitAsync(now = true) {
                kickPlayers(
                    config.message,
                    if (WhitelistManager.isEmpty) {
                        AVM.plugin.server.allPlayers
                    } else {
                        AVM.plugin.server.allPlayers.filter { it.uniqueId !in WhitelistManager.uuids }
                    }
                )
            }
        }
    }
}
