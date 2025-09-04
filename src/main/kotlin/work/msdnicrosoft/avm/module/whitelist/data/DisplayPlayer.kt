package work.msdnicrosoft.avm.module.whitelist.data

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.component.ComponentSerializer.MINI_MESSAGE
import work.msdnicrosoft.avm.util.component.clickToRunCommand
import work.msdnicrosoft.avm.util.component.clickToSuggestCommand
import work.msdnicrosoft.avm.util.component.hoverText
import work.msdnicrosoft.avm.util.component.tr

class DisplayPlayer(val player: Player) {
    private val playerUuid: String = player.uuid.toString()
    private val playerUsername: String = player.name

    val components: List<Component> = listOf(
        Component.text(playerUsername)
            .hoverText(
                Component.join(
                    JoinConfiguration.newlines(),
                    MINI_MESSAGE.deserialize("<gray>UUID: $playerUuid"),
                    Component.empty(),
                    tr("avm.whitelist.player.uuid.hover")
                )
            ).clickToSuggestCommand(playerUuid),
        MINI_MESSAGE.deserialize("<dark_gray>[<red>x<dark_gray>]")
            .hoverText(tr("avm.whitelist.player.username.hover"))
            .clickToRunCommand("/avmwl remove $playerUsername"),
        MINI_MESSAGE.deserialize("<dark_gray>:"),
    )

    fun build(): Component = Component.join(
        JoinConfiguration.spaces(),
        components + player.serverList.map { server ->
            val isServerGroup = config.isServerGroup(server)
            val hover = buildList {
                if (isServerGroup) {
                    add(tr("avm.whitelist.player.server.hover.1"))
                    val servers = config.getServersInGroup(server).joinToString(" ")
                    add(MINI_MESSAGE.deserialize("<reset>$servers"))
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

            MINI_MESSAGE.deserialize(
                "${if (isServerGroup) "<bold>" else ""}<server_name>",
                Placeholder.unparsed("server_name", server)
            )
                .hoverText(Component.join(JoinConfiguration.newlines(), hover))
                .clickToRunCommand("/avmwl remove $playerUsername $server")
        }
    )

    companion object {
        private inline val config
            get() = ConfigManager.config.whitelist
    }
}
