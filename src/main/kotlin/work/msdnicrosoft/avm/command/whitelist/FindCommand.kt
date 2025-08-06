package work.msdnicrosoft.avm.command.whitelist

import com.velocitypowered.api.command.CommandSource
import work.msdnicrosoft.avm.command.WhitelistCommand.sendWhitelistPlayers
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.PageTurner
import work.msdnicrosoft.avm.util.command.brigadier.*
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.server.task

object FindCommand {

    val command = literalCommand("find") {
        requires { hasPermission("avm.command.whitelist.find") }
        wordArgument("keyword") {
            suggests { builder ->
                buildSet {
                    addAll(WhitelistManager.usernames)
                    addAll(PlayerCache.readOnly)
                }.forEach(builder::suggest)
                builder.buildFuture()
            }
            executes {
                val keyword: String by this
                context.source.listFind(1, keyword)
                Command.SINGLE_SUCCESS
            }
            intArgument("page", min = 1) {
                executes {
                    val page: Int by this
                    val keyword: String by this
                    task { context.source.listFind(page, keyword) }
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
    private fun CommandSource.listFind(page: Int, keyword: String) {
        val result = WhitelistManager.find(keyword, page)

        if (result.isEmpty()) {
            this.sendTranslatable("avm.command.avmwl.find.empty")
            return
        }

        val maxPage = PageTurner.getMaxPage(result.size)
        if (page > maxPage) {
            this.sendTranslatable("avm.general.not.exist.page")
            return
        }

        if (page == 1) this.sendTranslatable("avm.command.avmwl.find.header")

        this.sendWhitelistPlayers(result)
        this.sendMessage(PageTurner("/avmwl find $keyword").build(page, maxPage))
    }
}
