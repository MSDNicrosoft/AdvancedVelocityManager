package work.msdnicrosoft.avm.command.whitelist

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import work.msdnicrosoft.avm.command.WhitelistCommand.sendWhitelistPlayers
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.*
import work.msdnicrosoft.avm.util.component.sendTranslatable

object FindCommand {

    val command: LiteralArgumentBuilder<CommandSource> = literal("find")
        .requires { source -> source.hasPermission("avm.command.whitelist.find") }
        .then(
            wordArgument("keyword")
                .suggests { context, builder ->
                    buildSet {
                        addAll(WhitelistManager.usernames)
                        addAll(PlayerCache.readOnly)
                    }.forEach(builder::suggest)
                    builder.buildFuture()
                }
                .executes { context ->
                    context.source.listFind(1, context.get<String>("keyword"))
                    Command.SINGLE_SUCCESS
                }
                .then(
                    intArgument("page")
                        .executes { context ->
                            val page = context.get<Int>("page")
                            if (page < 1) {
                                context.source.sendTranslatable("avm.whitelist.page.must.larger.than.zero")
                                return@executes Command.SINGLE_SUCCESS
                            }
                            context.source.listFind(page, context.get<String>("keyword"))
                            Command.SINGLE_SUCCESS
                        }
                )
        )

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
            sendTranslatable("avm.command.avmwl.find.empty")
            return
        }

        val maxPage = PageTurner.getMaxPage(result.size)
        if (page > maxPage) {
            sendTranslatable("avm.general.not.exist.page")
            return
        }

        if (page == 1) sendTranslatable("avm.command.avmwl.find.header")

        sendWhitelistPlayers(result)
        sendMessage(PageTurner("/avmwl find $keyword").build(page, maxPage))
    }
}
