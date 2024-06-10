package work.msdnicrosoft.avm.command

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*

@PlatformSide(Platform.VELOCITY)
@CommandHeader(
    name = "avm",
    permission = "avm.command"
)
object AVMCommand {

    @CommandBody(permission = "avm.command.reload")
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            // whitelist::reload
            sender.sendMessage("")
        }
    }

    @CommandBody(permission = "avm.command.status")
    val status = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            // whitelist::reload
            sender.sendMessage("")
        }
    }
}