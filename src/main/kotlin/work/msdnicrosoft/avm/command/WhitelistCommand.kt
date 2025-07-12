package work.msdnicrosoft.avm.command

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.util.isConsole
import taboolib.common.util.presentRun
import taboolib.module.chat.colored
import work.msdnicrosoft.avm.annotations.ShouldShow
import work.msdnicrosoft.avm.command.whitelist.AddCommand
import work.msdnicrosoft.avm.command.whitelist.ClearCommand
import work.msdnicrosoft.avm.command.whitelist.FindCommand
import work.msdnicrosoft.avm.command.whitelist.ListCommand
import work.msdnicrosoft.avm.command.whitelist.OffCommand
import work.msdnicrosoft.avm.command.whitelist.OnCommand
import work.msdnicrosoft.avm.command.whitelist.RemoveCommand
import work.msdnicrosoft.avm.command.whitelist.StatusCommand
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistPlayer
import work.msdnicrosoft.avm.util.ConfigUtil.getServersInGroup
import work.msdnicrosoft.avm.util.ConfigUtil.isServerGroup
import work.msdnicrosoft.avm.util.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.command.CommandUtil.buildHelper

@PlatformSide(Platform.VELOCITY)
@CommandHeader(name = "avmwl")
object WhitelistCommand {

    @ShouldShow("[page]")
    @CommandBody(permission = "avm.command.whitelist.list")
    val list = ListCommand.command

    @ShouldShow("<player>", "<server>", "[onlineMode]")
    @CommandBody(permission = "avm.command.whitelist.add")
    val add = AddCommand.command

    @ShouldShow("<player>", "[server]")
    @CommandBody(permission = "avm.command.whitelist.remove")
    val remove = RemoveCommand.command

    @ShouldShow
    @CommandBody(permission = "avm.command.whitelist.clear")
    val clear = ClearCommand.command

    @ShouldShow("<keyword>", "[page]")
    @CommandBody(permission = "avm.command.whitelist.find")
    val find = FindCommand.command

    @ShouldShow
    @CommandBody(permission = "avm.command.whitelist.on")
    val on = OnCommand.command

    @ShouldShow
    @CommandBody(permission = "avm.command.whitelist.off")
    val off = OffCommand.command

    @ShouldShow
    @CommandBody(permission = "avm.command.whitelist.status")
    val status = StatusCommand.command

    @CommandBody
    val main = mainCommand {
        buildHelper(this@WhitelistCommand::class)
    }

    /**
     * Sends a list of WhitelistManager.Players to the sender,
     * displaying their server list with appropriate formatting.
     *
     * @param players the list of players to send
     */
    fun ProxyCommandSender.sendWhitelistPlayers(players: List<WhitelistManager.Player>) {
        if (players.isEmpty()) return

        if (this.isConsole()) {
            players.forEach { player ->
                val servers = player.serverList.joinToString(" ") {
                    if (isServerGroup(it)) {
                        "&l$it&r(${getServersInGroup(it).joinToString(" ")}&r)"
                    } else {
                        "&r$it"
                    }
                }
                sendMessage("&7${player.name} &8:&r $servers".colored())
            }
        } else {
            getPlayer(this.name).presentRun {
                players.forEach { player ->
                    sendMessage(WhitelistPlayer(player, this@sendWhitelistPlayers).build())
                }
            }
        }
    }
}
