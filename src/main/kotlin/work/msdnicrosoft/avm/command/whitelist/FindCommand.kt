package work.msdnicrosoft.avm.command.whitelist

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.int
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.command.WhitelistCommand.sendWhitelistPlayers
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.PageTurner

@PlatformSide(Platform.VELOCITY)
object FindCommand {
    val command = subCommand {
        dynamic("keyword") {
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ ->
                buildSet {
                    addAll(WhitelistManager.usernames)
                    addAll(PlayerCache.readOnly)
                }.toList()
            }
            int("page") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    val page = context.int("page")
                    if (page < 1) {
                        sender.sendLang("whitelist-page-must-larger-than-zero")
                        return@execute
                    }
                    sender.listFind(page, context["keyword"])
                }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                sender.listFind(1, context["keyword"])
            }
        }
    }

    /**
     * Sends a message to the sender with the header for the whitelist find,
     * then sends a message for each player found on the specified page.
     * Finally, sends a message with the footer for the whitelist find.
     *
     * @param page The page number to retrieve.
     * @param keyword The keyword to search for.
     */
    private fun ProxyCommandSender.listFind(page: Int, keyword: String) {
        val result = WhitelistManager.find(keyword, page)

        if (result.isEmpty()) {
            sendLang("command-avmwl-find-empty")
            return
        }

        val maxPage = PageTurner.getMaxPage(result.size)
        if (page > maxPage) {
            sendLang("general-page-not-found")
            return
        }

        if (page == 1) sendLang("command-avmwl-find-header")

        sendWhitelistPlayers(result)
        PageTurner(this, "/avmwl find $keyword")
            .build(page, maxPage)
            .sendTo(this)
    }
}
