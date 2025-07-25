package work.msdnicrosoft.avm.module.whitelist

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.asLangText
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager.Player
import work.msdnicrosoft.avm.util.ConfigUtil.getServersInGroup
import work.msdnicrosoft.avm.util.ConfigUtil.isServerGroup
import work.msdnicrosoft.avm.util.component.ComponentUtil.createClickEvent
import work.msdnicrosoft.avm.util.component.ComponentUtil.createHoverEvent
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.Format
import work.msdnicrosoft.avm.util.string.replace

class WhitelistPlayer(val player: Player, val sender: ProxyCommandSender) {

    private val formats: List<Format> = listOf(
        Format(
            text = "<player_name>",
            hover = listOf(
                "<gray>UUID: <player_uuid>",
                "",
                sender.asLangText("whitelist-each-player-uuid-hover"),
            ),
            suggest = "<player_uuid>"
        ),
        Format(
            text = "<dark_gray>[<red>x<dark_gray>]",
            hover = listOf(sender.asLangText("whitelist-each-player-username-hover")),
            command = "/avmwl remove <player_name>"
        ),
        Format(text = "<dark_gray>:"),
    )

    private val playerUsername = player.name
    private val playerUuid = player.uuid.toString()

    /**
     * Deserialize the message by replacing placeholders with actual values.
     * @return The deserialized message with placeholders replaced.
     */
    private fun String.deserialize(serverName: String? = null): Component =
        miniMessage.deserialize(
            this,
            Placeholder.parsed("player_name", playerUsername),
            Placeholder.parsed("player_uuid", playerUuid),
            Placeholder.parsed("server_name", serverName.orEmpty()),
        )

    /**
     * Replace placeholders in the message with actual player and server information.
     * @return The message with placeholders replaced.
     */
    private fun String.replacePlaceholders(serverName: String? = null): String = this.replace(
        "<player_name>" to playerUsername,
        "<player_uuid>" to playerUuid,
        "<server_name>" to serverName.orEmpty()
    )

    fun build(): Component = Component.join(
        JoinConfiguration.spaces(),
        formats.map { format ->
            format.text.deserialize()
                .hoverEvent(createHoverEvent(format) { deserialize() })
                .clickEvent(createClickEvent(format) { replacePlaceholders() })
        } + player.serverList.map { server ->
            val isServerGroup = isServerGroup(server)
            val serverFormat = Format(
                text = "${if (isServerGroup) "<bold>" else ""}<server_name>",
                hover = buildList {
                    if (isServerGroup) {
                        add(sender.asLangText("whitelist-each-player-server-hover-1"))
                        add("<reset>${getServersInGroup(server).joinToString(" ")}")
                        add("")
                    }
                    add(sender.asLangText("whitelist-each-player-server-hover-2"))
                },
                command = "/avmwl remove <player_name> <server_name>"
            )
            serverFormat.text.deserialize(server)
                .hoverEvent(createHoverEvent(serverFormat) { deserialize(server) })
                .clickEvent(createClickEvent(serverFormat) { replacePlaceholders(server) })
        }
    )
}
