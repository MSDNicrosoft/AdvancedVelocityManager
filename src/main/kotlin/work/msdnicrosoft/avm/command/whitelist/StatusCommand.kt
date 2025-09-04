package work.msdnicrosoft.avm.command.whitelist

import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.builder.Command
import work.msdnicrosoft.avm.util.command.builder.executes
import work.msdnicrosoft.avm.util.command.builder.literalCommand
import work.msdnicrosoft.avm.util.command.builder.requires
import work.msdnicrosoft.avm.util.component.tr

object StatusCommand {
    private inline val config get() = ConfigManager.config.whitelist

    val command = literalCommand("status") {
        requires { hasPermission("avm.command.whitelist.status") }
        executes {
            val state = if (WhitelistManager.enabled) "on" else "off"
            sendTranslatable(
                "avm.command.avmwl.list.header",
                Argument.numeric("player", WhitelistManager.size)
            )
            sendTranslatable(
                "avm.command.avmwl.status.state",
                Argument.component("state", tr("avm.general.$state"))
            )
            sendTranslatable(
                "avm.command.avmwl.status.cache",
                Argument.numeric("current", PlayerCache.readOnly.size),
                Argument.numeric("total", config.cachePlayers.maxSize)
            )
            Command.SINGLE_SUCCESS
        }
    }
}
