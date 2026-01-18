package work.msdnicrosoft.avm.command.whitelist

import work.msdnicrosoft.avm.command.WhitelistCommand.sendWhitelistPlayers
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager.Player
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.CommandContext
import work.msdnicrosoft.avm.util.component.widget.Paginator
import work.msdnicrosoft.avm.util.server.task

object FindCommand {
    val command = literalCommand("find") {
        requires { hasPermission("avm.command.whitelist.find") }
        wordArgument("keyword") {
            suggests { builder ->
                WhitelistManager.usernames.forEach(builder::suggest)
                PlayerCache.readOnly.forEach(builder::suggest)
                builder.buildFuture()
            }
            executes {
                val keyword: String by this
                listFind(1, keyword)
                Command.SINGLE_SUCCESS
            }
            intArgument("page", min = 1) {
                executes {
                    val page: Int by this
                    val keyword: String by this
                    task { listFind(page, keyword) }
                    Command.SINGLE_SUCCESS
                }
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
    private fun CommandContext.listFind(page: Int, keyword: String) {
        val result: List<Player> = WhitelistManager.find(keyword, page)

        if (result.isEmpty()) {
            sendTranslatable("avm.command.avmwl.find.empty")
            return
        }

        val maxPage: Int = Paginator.getMaxPage(result.size)
        if (page > maxPage) {
            sendTranslatable("avm.general.not_found.page")
            return
        }

        if (page == 1) sendTranslatable("avm.command.avmwl.find.header")

        context.source.sendWhitelistPlayers(result)
        sendMessage(Paginator("/avmwl find $keyword").toComponent(page, maxPage))
    }
}
