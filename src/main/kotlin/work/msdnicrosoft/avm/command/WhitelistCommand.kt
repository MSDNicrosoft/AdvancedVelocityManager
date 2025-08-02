package work.msdnicrosoft.avm.command

import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import work.msdnicrosoft.avm.annotations.CommandNode
import work.msdnicrosoft.avm.annotations.RootCommand
import work.msdnicrosoft.avm.command.whitelist.*
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistPlayer
import work.msdnicrosoft.avm.util.ConfigUtil.getServersInGroup
import work.msdnicrosoft.avm.util.ConfigUtil.isServerGroup
import work.msdnicrosoft.avm.util.command.*

@RootCommand("avmwl")
object WhitelistCommand {

    fun init() {
        command.register()
    }

    fun disable() {
        command.unregister()
    }

    @CommandNode("add", "<player>", "<server>", "[onlineMode]")
    val add = AddCommand.command

    @CommandNode("clear")
    val clear = ClearCommand.command

    @CommandNode("find", "<keyword>", "[page]")
    val find = FindCommand.command

    @CommandNode("list", "[page]")
    val list = ListCommand.command

    @CommandNode("off")
    val off = OffCommand.command

    @CommandNode("on")
    val on = OnCommand.command

    @CommandNode("remove", "<player>", "[server]")
    val remove = RemoveCommand.command

    @CommandNode("status")
    val status = StatusCommand.command

    val command: LiteralCommandNode<CommandSource> = literal("avmwl")
        .executes { context -> context.buildHelp(this@WhitelistCommand.javaClass) }
        .then(add)
        .then(clear)
        .then(find)
        .then(list)
        .then(off)
        .then(on)
        .then(remove)
        .then(status)
        .build()

    /**
     * Sends a list of WhitelistManager.Players to the sender,
     * displaying their server list with appropriate formatting.
     *
     * @param players the list of players to send
     */
    fun CommandSource.sendWhitelistPlayers(players: List<WhitelistManager.Player>) {
        if (players.isEmpty()) return

        if (this.isConsole) {
            players.forEach { player ->
                val servers = player.serverList.joinToString(" ") {
                    if (isServerGroup(it)) {
                        "<bold>$it<reset>(${getServersInGroup(it).joinToString(" ")}<reset>)"
                    } else {
                        "<reset>$it"
                    }
                }
                this.sendRichMessage("<gray>${player.name} <dark_gray>:<reset> $servers")
            }
        } else {
            players.forEach { player ->
                this.sendMessage(WhitelistPlayer(player).build())
            }
        }
    }
}
