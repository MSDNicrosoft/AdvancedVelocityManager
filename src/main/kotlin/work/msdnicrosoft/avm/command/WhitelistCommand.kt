package work.msdnicrosoft.avm.command

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import work.msdnicrosoft.avm.annotations.command.CommandNode
import work.msdnicrosoft.avm.annotations.command.RootCommand
import work.msdnicrosoft.avm.command.whitelist.*
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.data.Player
import work.msdnicrosoft.avm.util.command.builder.executes
import work.msdnicrosoft.avm.util.command.builder.literalCommand
import work.msdnicrosoft.avm.util.command.builder.then
import work.msdnicrosoft.avm.util.command.context.buildHelp
import work.msdnicrosoft.avm.util.command.context.isConsole
import work.msdnicrosoft.avm.util.command.context.sendMessage
import work.msdnicrosoft.avm.util.command.register
import work.msdnicrosoft.avm.util.command.unregister
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.component.builder.text.component
import work.msdnicrosoft.avm.util.component.widget.Button
import work.msdnicrosoft.avm.util.component.widget.button

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
        players.forEach { player ->
            sendMessage(JoinConfiguration.noSeparators()) {
                text(player.name) styled {
                    hoverText {
                        component(JoinConfiguration.newlines()) {
                            text("UUID: ${player.uuid}") styled { color(NamedTextColor.GRAY) }
                            empty()
                            translatable("avm.whitelist.player.uuid.hover")
                        }
                    }
                    click { suggestCommand(player.uuid.toString()) }
                }
                if (!this@sendWhitelistPlayers.isConsole) {
                    componentLike(
                        button("x") {
                            borderType(Button.BorderType.SQUARE)
                            color {
                                enabled(NamedTextColor.RED)
                                border(NamedTextColor.DARK_GRAY)
                            }
                            hover { whenEnabled(tr("avm.whitelist.player.username.hover")) }
                            click { whenEnabled { runCommand("/avmwl remove ${player.name}") } }
                        }
                    )
                }
                text(": ") styled { color(NamedTextColor.DARK_GRAY) }
                player.serverList.forEach { server ->
                    val isServerGroup: Boolean = config.isServerGroup(server)

                    text(server) styled {
                        bold(isServerGroup)
                        hoverText {
                            component(JoinConfiguration.newlines()) {
                                if (isServerGroup) {
                                    translatable("avm.whitelist.player.server.hover.1")
                                    config.getServersInGroup(server).forEach(this::text)
                                    empty()
                                }
                                translatable("avm.whitelist.player.server.hover.2") {
                                    args {
                                        string("server_name", server)
                                        string("player_name", player.name)
                                    }
                                }
                            }
                        }
                        click { runCommand("/avmwl remove ${player.uuid} $server") }
                    }
                    if (this@sendWhitelistPlayers.isConsole && isServerGroup) {
                        text("(")
                        config.getServersInGroup(server).forEach(this::text)
                        text(")")
                    }
                }
            }
        }
    }
}
