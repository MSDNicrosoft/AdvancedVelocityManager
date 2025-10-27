package work.msdnicrosoft.avm.command.whitelist

import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.builder.Command
import work.msdnicrosoft.avm.util.command.builder.executes
import work.msdnicrosoft.avm.util.command.builder.literalCommand
import work.msdnicrosoft.avm.util.command.builder.requires
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr

object OffCommand {
    val command = literalCommand("off") {
        requires { hasPermission("avm.command.whitelist.off") }
        executes {
            WhitelistManager.enabled = false
            sendTranslatable("avm.command.avmwl.status.state") {
                args { component("state", tr("avm.general.off")) }
            }
            Command.SINGLE_SUCCESS
        }
    }
}
