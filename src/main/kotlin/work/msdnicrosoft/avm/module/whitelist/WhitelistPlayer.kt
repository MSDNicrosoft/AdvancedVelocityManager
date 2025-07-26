package work.msdnicrosoft.avm.module.whitelist

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager.Player
import work.msdnicrosoft.avm.util.ConfigUtil.getServersInGroup
import work.msdnicrosoft.avm.util.ConfigUtil.isServerGroup
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.tr

class WhitelistPlayer(val player: Player) {
    private val playerUuid = player.uuid.toString()
    private val playerUsername = player.name

    val components = listOf(
        Component.text(playerUsername)
            .hoverEvent(
                HoverEvent.showText(
                    Component.join(
                        JoinConfiguration.newlines(),
                        miniMessage.deserialize("<gray>UUID: $playerUuid"),
                        Component.empty(),
                        tr("avm.whitelist.player.uuid.hover")
                    )
                )
            ).clickEvent(ClickEvent.suggestCommand(playerUuid)),
        miniMessage.deserialize("<dark_gray>[<red>x<dark_gray>]")
            .hoverEvent(HoverEvent.showText(tr("avm.whitelist.player.username.hover")))
            .clickEvent(ClickEvent.runCommand("/avmwl remove $playerUsername")),
        miniMessage.deserialize("<dark_gray>:"),
    )

    fun build(): Component = Component.join(
        JoinConfiguration.spaces(),
        components + player.serverList.map { server ->
            val isServerGroup = isServerGroup(server)
            val hover = buildList {
                if (isServerGroup) {
                    add(tr("avm.whitelist.player.server.hover.1"))
                    add(miniMessage.deserialize("<reset>${getServersInGroup(server).joinToString(" ")}"))
                    add(Component.empty())
                }
                add(
                    tr(
                        "avm.whitelist.player.server.hover.2",
                        Argument.string("server_name", server),
                        Argument.string("player_name", playerUsername)
                    )
                )
            }

            miniMessage.deserialize(
                "${if (isServerGroup) "<bold>" else ""}<server_name>",
                Placeholder.unparsed("server_name", server)
            ).hoverEvent(HoverEvent.showText(Component.join(JoinConfiguration.newlines(), hover)))
                .clickEvent(ClickEvent.runCommand("/avmwl remove $playerUsername $server"))
        }
    )
}
