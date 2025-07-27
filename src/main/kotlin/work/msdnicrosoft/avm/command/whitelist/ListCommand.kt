package work.msdnicrosoft.avm.command.whitelist

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.command.WhitelistCommand.sendWhitelistPlayers
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.*

object ListCommand {

    val command: LiteralArgumentBuilder<CommandSource> = literal("list")
        .requires { source -> source.hasPermission("avm.command.whitelist.list") }
        .executes { context ->
            context.source.listWhitelist(1)
            Command.SINGLE_SUCCESS
        }
        .then(
            intArgument("page")
                .suggests { context, builder ->
                    for (page in 1..WhitelistManager.maxPage) builder.suggest(page)
                    builder.buildFuture()
                }.executes { context ->
                    val page = context.getInt("page")
                    if (page < 1) {
                        context.source.sendTranslatable("avm.whitelist.page.must.larger.than.zero")
                        return@executes Command.SINGLE_SUCCESS
                    }

                    context.source.listWhitelist(page)

                    Command.SINGLE_SUCCESS
                }
        )

    private fun CommandSource.listWhitelist(page: Int) {
        if (WhitelistManager.isEmpty) {
            sendTranslatable("avm.command.avmwl.list.empty")
            return
        }
        val maxPage = WhitelistManager.maxPage
        if (page > maxPage) {
            sendTranslatable("avm.general.not.exist.page")
            return
        }
        if (page == 1) {
            sendTranslatable(
                "avm.command.avmwl.list.header",
                Argument.numeric("player", WhitelistManager.size)
            )
        }

        sendWhitelistPlayers(WhitelistManager.pageOf(page))

        sendMessage(PageTurner("/avmwl list").build(page, maxPage))
    }
}
