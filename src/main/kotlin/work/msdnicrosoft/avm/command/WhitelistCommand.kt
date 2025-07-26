package work.msdnicrosoft.avm.command

import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import work.msdnicrosoft.avm.command.whitelist.*
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistPlayer
import work.msdnicrosoft.avm.util.ConfigUtil.getServersInGroup
import work.msdnicrosoft.avm.util.ConfigUtil.isServerGroup
import work.msdnicrosoft.avm.util.command.isConsole
import work.msdnicrosoft.avm.util.command.literal
import work.msdnicrosoft.avm.util.command.register
import work.msdnicrosoft.avm.util.command.unregister
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage

object WhitelistCommand {

    fun init() {
        command.register()
    }

    fun disable() {
        command.unregister()
    }

    val command: LiteralCommandNode<CommandSource> = literal("avmwl")
        .then(AddCommand.command)
        .then(ClearCommand.command)
        .then(FindCommand.command)
        .then(ListCommand.command)
        .then(OffCommand.command)
        .then(OnCommand.command)
        .then(RemoveCommand.command)
        .then(StatusCommand.command)
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
                sendMessage(miniMessage.deserialize("<gray>${player.name} <dark_gray>:<reset> $servers"))
            }
        } else {
            players.forEach { player ->
                sendMessage(WhitelistPlayer(player).build())
            }
        }
    }
}
