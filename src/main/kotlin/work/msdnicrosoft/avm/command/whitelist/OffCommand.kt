package work.msdnicrosoft.avm.command.whitelist

import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.brigadier.Command
import work.msdnicrosoft.avm.util.command.brigadier.executes
import work.msdnicrosoft.avm.util.command.brigadier.literalCommand
import work.msdnicrosoft.avm.util.command.brigadier.requires
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr

object OffCommand {
    val command = literalCommand("off") {
        requires { hasPermission("avm.command.whitelist.off") }
        executes {
            WhitelistManager.enabled = false
            context.source.sendTranslatable(
                "avm.command.avmwl.status.state",
                Argument.component("state", tr("avm.general.off"))
            )
            Command.SINGLE_SUCCESS
        }
    }
}
