package work.msdnicrosoft.avm.command.whitelist

import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.builder.Command
import work.msdnicrosoft.avm.util.command.builder.executes
import work.msdnicrosoft.avm.util.command.builder.literalCommand
import work.msdnicrosoft.avm.util.command.builder.requires
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
