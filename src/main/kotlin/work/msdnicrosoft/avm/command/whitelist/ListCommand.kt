package work.msdnicrosoft.avm.command.whitelist

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.int
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.command.WhitelistCommand.sendWhitelistPlayers
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.PageTurner

@PlatformSide(Platform.VELOCITY)
object ListCommand {
    val command = subCommand {
        dynamic("page") {
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                (1..WhitelistManager.maxPage).map { it.toString() }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                val page = context.int("page")
                if (page < 1) {
                    sender.sendLang("whitelist-page-must-larger-than-zero")
                    return@execute
                }
                sender.listWhitelist(page)
            }
        }
        execute<ProxyCommandSender> { sender, _, _ ->
            sender.listWhitelist(1)
        }
    }

    private fun ProxyCommandSender.listWhitelist(page: Int) {
        if (WhitelistManager.isEmpty) {
            sendLang("command-avmwl-list-empty")
            return
        }
        val maxPage = WhitelistManager.maxPage
        if (page > maxPage) {
            sendLang("general-page-not-found")
            return
        }
        if (page == 1) sendLang("command-avmwl-list-header", WhitelistManager.size)

        sendWhitelistPlayers(WhitelistManager.pageOf(page))

        PageTurner(this, "/avmwl list")
            .build(page, maxPage)
            .sendTo(this)
    }
}
