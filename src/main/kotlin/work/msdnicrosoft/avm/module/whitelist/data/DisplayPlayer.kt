package work.msdnicrosoft.avm.module.whitelist.data

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.util.ConfigUtil
import work.msdnicrosoft.avm.util.component.serializer.SerializationType
import work.msdnicrosoft.avm.util.component.tr

class DisplayPlayer(val player: Player) {
    private val playerUuid = player.uuid.toString()
    private val playerUsername = player.name

    val components = listOf(
        Component.text(playerUsername)
            .hoverEvent(
                HoverEvent.showText(
                    Component.join(
                        JoinConfiguration.newlines(),
                        SerializationType.MINI_MESSAGE.deserialize("<gray>UUID: $playerUuid"),
                        Component.empty(),
                        tr("avm.whitelist.player.uuid.hover")
                    )
                )
            ).clickEvent(ClickEvent.suggestCommand(playerUuid)),
        SerializationType.MINI_MESSAGE.deserialize("<dark_gray>[<red>x<dark_gray>]")
            .hoverEvent(HoverEvent.showText(tr("avm.whitelist.player.username.hover")))
            .clickEvent(ClickEvent.runCommand("/avmwl remove $playerUsername")),
        SerializationType.MINI_MESSAGE.deserialize("<dark_gray>:"),
    )

    fun build(): Component = Component.join(
        JoinConfiguration.spaces(),
        components + player.serverList.map { server ->
            val isServerGroup = ConfigUtil.isServerGroup(server)
            val hover = buildList {
                if (isServerGroup) {
                    add(tr("avm.whitelist.player.server.hover.1"))
                    val servers = ConfigUtil.getServersInGroup(server).joinToString(" ")
                    add(SerializationType.MINI_MESSAGE.deserialize("<reset>$servers"))
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

            SerializationType.MINI_MESSAGE.deserialize(
                "${if (isServerGroup) "<bold>" else ""}<server_name>",
                Placeholder.unparsed("server_name", server)
            ).hoverEvent(HoverEvent.showText(Component.join(JoinConfiguration.newlines(), hover)))
                .clickEvent(ClickEvent.runCommand("/avmwl remove $playerUsername $server"))
        }
    )
}
