package work.msdnicrosoft.avm.command.whitelist

import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.command.WhitelistCommand.sendWhitelistPlayers
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.PageTurner
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.CommandContext

object ListCommand {

    val command = literalCommand("list") {
        requires { hasPermission("avm.command.whitelist.list") }
        executes {
            listWhitelist(1)
            Command.SINGLE_SUCCESS
        }
        intArgument("page", min = 1) {
            suggests { builder ->
                for (page in 1..WhitelistManager.maxPage) builder.suggest(page)
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
            this.sendTranslatable("avm.command.avmwl.list.empty")
            return
        }
        val maxPage = WhitelistManager.maxPage
        if (page > maxPage) {
            this.sendTranslatable("avm.general.not.exist.page")
            return
        }
        if (page == 1) {
            this.sendTranslatable(
                "avm.command.avmwl.list.header",
                Argument.numeric("player", WhitelistManager.size)
            )
        }

        this.context.source.sendWhitelistPlayers(WhitelistManager.pageOf(page))

        this.sendMessage(PageTurner("/avmwl list").build(page, maxPage))
    }
}
