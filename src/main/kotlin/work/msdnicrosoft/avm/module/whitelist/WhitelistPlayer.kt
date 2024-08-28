package work.msdnicrosoft.avm.module.whitelist

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.asLangText
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager.Player
import work.msdnicrosoft.avm.util.ConfigUtil.getServersInGroup
import work.msdnicrosoft.avm.util.ConfigUtil.isServerGroup
import work.msdnicrosoft.avm.util.StringUtil.replace
import work.msdnicrosoft.avm.util.component.ComponentUtil.createClickEvent
import work.msdnicrosoft.avm.util.component.ComponentUtil.createHoverEvent
import work.msdnicrosoft.avm.util.component.ComponentUtil.serializer
import work.msdnicrosoft.avm.util.component.Format

class WhitelistPlayer(val player: Player, val sender: ProxyCommandSender) {

    private val formats: List<Format> = listOf(
        Format(
            text = "%player_name%",
            hover = listOf(
                "&7UUID: %player_uuid%",
                "",
                sender.asLangText("whitelist-each-player-uuid-hover"),
            ),
            suggest = "%player_uuid%"
        ),
        Format(
            text = "&8[&cx&8]",
            hover = listOf(sender.asLangText("whitelist-each-player-username-hover")),
            command = "/avmwl remove %player_name%"
        ),
        Format("&8:"),
    )

    val playerUsername = player.name
    val playerUuid = player.uuid.toString()

    /**
     * Deserialize the message by replacing placeholders with actual values.
     * @return The deserialized message with placeholders replaced.
     */
    private fun String.deserialize(serverName: String? = null) = serializer.buildComponent(this)
        .replace("%player_name%", playerUsername)
        .replace("%player_uuid%", playerUuid)
        .let { if (serverName != null) it.replace("%server_name%", serverName) else it }
        .build()

    /**
     * Replace placeholders in the message with actual player and server information.
     * @return The message with placeholders replaced.
     */
    private fun String.replacePlaceholders(serverName: String? = null) = this.replace(
        "%player_name%" to playerUsername,
        "%player_uuid%" to playerUuid
    ).let { if (serverName != null) it.replace("%server_name%" to serverName) else it }

    fun build() = Component.join(
        JoinConfiguration.spaces(),
        formats.map { format ->
            format.text.deserialize()
                .hoverEvent(createHoverEvent(format) { deserialize() })
                .clickEvent(createClickEvent(format) { replacePlaceholders() })
        } + player.serverList.map { server ->
            val isServerGroup = isServerGroup(server)
            val serverFormat = Format(
                text = "${if (isServerGroup) "&l" else ""}%server_name%",
                hover = buildList {
                    if (isServerGroup) {
                        add(sender.asLangText("whitelist-each-player-server-hover-1"))
                        add("&r${getServersInGroup(server)!!.joinToString(" ")}")
                        add(" ")
                    }
                    add(sender.asLangText("whitelist-each-player-server-hover-2"))
                },
                command = "/avmwl remove %player_name% %server_name%"
            )
            serverFormat.text.deserialize(server)
                .hoverEvent(createHoverEvent(serverFormat) { deserialize(server) })
                .clickEvent(createClickEvent(serverFormat) { replacePlaceholders(server) })
        }
    )
}
