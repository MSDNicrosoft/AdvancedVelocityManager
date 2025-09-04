package work.msdnicrosoft.avm.command

import com.velocitypowered.api.command.CommandSource
import work.msdnicrosoft.avm.annotations.CommandNode
import work.msdnicrosoft.avm.annotations.RootCommand
import work.msdnicrosoft.avm.command.whitelist.*
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.data.DisplayPlayer
import work.msdnicrosoft.avm.module.whitelist.data.Player
import work.msdnicrosoft.avm.util.command.builder.executes
import work.msdnicrosoft.avm.util.command.builder.literalCommand
import work.msdnicrosoft.avm.util.command.builder.then
import work.msdnicrosoft.avm.util.command.context.buildHelp
import work.msdnicrosoft.avm.util.command.context.isConsole
import work.msdnicrosoft.avm.util.command.register
import work.msdnicrosoft.avm.util.command.unregister

@RootCommand("avmwl")
object WhitelistCommand {

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

    private inline val config get() = ConfigManager.config.whitelist

    val command = literalCommand("avmwl") {
        executes { buildHelp(this@WhitelistCommand.javaClass) }
        then(add)
        then(clear)
        then(find)
        then(list)
        then(off)
        then(on)
        then(remove)
        then(status)
    }.build()

    fun init() {
        this.command.register()
    }

    fun disable() {
        this.command.unregister()
    }

    /**
     * Sends a list of WhitelistManager.Players to the sender,
     * displaying their server list with appropriate formatting.
     *
     * @param players the list of players to send
     */
    fun CommandSource.sendWhitelistPlayers(players: List<Player>) {
        if (players.isEmpty()) return

        if (this.isConsole) {
            players.forEach { player ->
                val servers: String = player.serverList.joinToString(" ") { server ->
                    if (config.isServerGroup(server)) {
                        "<bold>$server<reset>(${config.getServersInGroup(server).joinToString(" ")}<reset>)"
                    } else {
                        "<reset>$server"
                    }
                }
                this.sendRichMessage("<gray>${player.name} <dark_gray>:<reset> $servers")
            }
        } else {
            players.forEach { player ->
                this.sendMessage(DisplayPlayer(player).build())
            }
        }
    }
}
