package work.msdnicrosoft.avm.command.whitelist

import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.builder.Command
import work.msdnicrosoft.avm.util.command.builder.executes
import work.msdnicrosoft.avm.util.command.builder.literalCommand
import work.msdnicrosoft.avm.util.command.builder.requires
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.util.server.task

object OnCommand {
    private inline val config get() = ConfigManager.config.whitelist

    val command = literalCommand("on") {
        requires { hasPermission("avm.command.whitelist.on") }
        executes {
            WhitelistManager.enabled = true
            sendTranslatable(
                "avm.command.avmwl.status.state",
                Argument.component("state", tr("avm.general.on"))
            )
            task {
                kickPlayers(
                    config.message,
                    if (WhitelistManager.isEmpty) {
                        server.allPlayers
                    } else {
                        server.allPlayers.filter { it.uniqueId !in WhitelistManager.uuids }
                    }
                )
            }
            Command.SINGLE_SUCCESS
        }
    }
}
