package work.msdnicrosoft.avm.command

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*

@PlatformSide(Platform.VELOCITY)
@CommandHeader(
    name = "avmwl",
    permission = "avm.command.whitelist"
)
object AVMWhitelistCommand {

    @CommandBody(permission = "avm.command.whitelist.list")
    val list = subCommand {
        int("page") {

        }
        execute<ProxyCommandSender> { sender, _, _ ->
            // list = whitelist::getAll()
            // format(list)
        }
    }

    @CommandBody(permission = "avm.command.whitelist.add")
    val add = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender>(uncheck = true) { sender, context ->
                // Suggestion Priority:
                // whitelist::getCachedPlayerList, players who attempted to join the server
                // ...
                listOf()
            }
            dynamic("server") {
                suggestion<ProxyCommandSender>(uncheck = true) { sender, context ->
                    // Return backend servers
                    listOf()
                }
                execute<ProxyCommandSender> { sender, context, argument ->
                }
            }
            execute<ProxyCommandSender> { sender, context, argument ->
                // whitelist:add, add player to fallback server
            }
        }
    }

    @CommandBody(permission = "avm.command.whitelist.remove")
    val remove = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender>(uncheck = false) { sender, context ->
                // whitelist::getAll
                listOf()
            }
            execute<ProxyCommandSender> { sender, _, _ ->
                // whitelist::remove
                sender.sendMessage("")
            }
        }
    }

    @CommandBody(permission = "avm.command.whitelist.clear")
    val clear = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            // whitelist::clear
            // need confirmation !!!
            sender.sendMessage("")
        }
    }

    @CommandBody(permission = "avm.command.whitelist.find")
    val find = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender>(uncheck = true) { sender, context ->
                // Suggestion Priority:
                // whitelist::getCachedPlayerList players who attempted to join the server
                // ...
                listOf()
            }
            int("page") {

            }
            execute<ProxyCommandSender> { sender, context, argument ->
                // whitelist:add
            }
        }
    }

    @CommandBody(permission = "avm.command.whitelist.on")
    val on = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            // whitelist::on
            sender.sendMessage("")
        }
    }

    @CommandBody(permission = "avm.command.whitelist.off")
    val off = subCommand {
        // whitelist::off
        execute<ProxyCommandSender> { sender, _, _ ->
            sender.sendMessage("")
        }
    }

    @CommandBody(permission = "avm..whitelist.reload")
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            // whitelist::reload
            sender.sendMessage("")
        }
    }

    @CommandBody(permission = "avm.command.whitelist.status")
    val status = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            // whitelist::reload
            sender.sendMessage("")
        }
    }
}