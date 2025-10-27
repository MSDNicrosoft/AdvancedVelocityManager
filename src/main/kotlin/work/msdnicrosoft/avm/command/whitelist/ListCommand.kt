package work.msdnicrosoft.avm.command.whitelist

import work.msdnicrosoft.avm.command.WhitelistCommand.sendWhitelistPlayers
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.CommandContext
import work.msdnicrosoft.avm.util.component.widget.Paginator

object ListCommand {
    val command = literalCommand("list") {
        requires { hasPermission("avm.command.whitelist.list") }
        executes {
            listWhitelist(1)
            Command.SINGLE_SUCCESS
        }
        intArgument("page", min = 1) {
            suggests { builder ->
                for (page: Int in 1..WhitelistManager.maxPage) builder.suggest(page)
                builder.buildFuture()
            }
            executes {
                val page: Int by this
                listWhitelist(page)
                Command.SINGLE_SUCCESS
            }
        }
    }

    private fun CommandContext.listWhitelist(page: Int) {
        if (WhitelistManager.isEmpty) {
            sendTranslatable("avm.command.avmwl.list.empty")
            return
        }
        val maxPage: Int = WhitelistManager.maxPage
        if (page > maxPage) {
            sendTranslatable("avm.general.not_found.page")
            return
        }
        if (page == 1) {
            sendTranslatable("avm.command.avmwl.list.header") {
                args { numeric("player", WhitelistManager.size) }
            }
        }

        context.source.sendWhitelistPlayers(WhitelistManager.pageOf(page))

        sendMessage(Paginator("/avmwl list").toComponent(page, maxPage))
    }
}
