package work.msdnicrosoft.avm.module.chatbridge

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.DateTimeUtil.getDateTime
import work.msdnicrosoft.avm.util.component.ComponentSerializer.STYLE_ONLY_MINI_MESSAGE
import work.msdnicrosoft.avm.util.component.ComponentUtil.createClickEvent
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.TIMEOUT_PING_RESULT
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Represents a chat message sent by a player. This class is used to format
 * the message based on the configuration specified in the `chat-bridge` section
 * of the main configuration file.
 *
 * @param player The player who sent the message.
 * @param message The message content.
 */
class ChatMessage(player: Player, private val message: String) {
    private val server = player.currentServer.get()
    private val serverName = server.serverInfo.name
    private val serverNickname = getServerNickname(serverName)
    private val serverOnlinePlayers = server.server.playersConnected.size.toString()

    private val playerUsername = player.username
    private val playerUuid = player.uniqueId.toString()
    private val playerPing = player.ping.toString()

    private val serverPing = try {
        server.server.ping().get(20, TimeUnit.MILLISECONDS)
    } catch (_: TimeoutException) {
        TIMEOUT_PING_RESULT
    }
    private val serverVersion = serverPing.version.name

    /**
     * Build the final chat message with all specified formats and events.
     * @return The built chat message with all formats and events applied.
     */
    fun build() = Component.join(
        JoinConfiguration.noSeparators(),
        config.publicChatFormat.map { format ->
            format.text.deserialize()
                .hoverEvent(format.hover?.joinToString("\n")?.deserialize()?.let { HoverEvent.showText(it) })
                .clickEvent(createClickEvent(format) { replacePlaceholders() })
        }
    )

    /**
     * Deserialize the message by replacing placeholders with actual values.
     * @return The deserialized message with placeholders replaced.
     */
    private fun String.deserialize() = STYLE_ONLY_MINI_MESSAGE.deserialize(
        this,
        Placeholder.unparsed("player_name", playerUsername),
        Placeholder.unparsed("player_uuid", playerUuid),
        Placeholder.unparsed("player_ping", playerPing),
        Placeholder.unparsed("server_name", serverName),
        Placeholder.unparsed("server_nickname", serverNickname),
        Placeholder.unparsed("server_online_players", serverOnlinePlayers),
        Placeholder.unparsed("server_version", serverVersion),
        Placeholder.unparsed("player_message_sent_time", getDateTime()),
        if (config.allowFormatCode) {
            Placeholder.parsed("player_message", message)
        } else {
            Placeholder.unparsed("player_message", message)
        },
    )

    /**
     * Replace placeholders in the message with actual player and server information.
     * @return The message with placeholders replaced.
     */
    private fun String.replacePlaceholders() =
        this.replace("<player_name>", playerUsername)
            .replace("<player_uuid>", playerUuid)
            .replace("<player_ping>", playerPing)
            .replace("<player_message>", message)
            .replace("<server_name>", serverName)
            .replace("<server_nickname>", serverNickname)
            .replace("<server_online_players>", serverOnlinePlayers)
            .replace("<server_version>", serverVersion)

    companion object {
        private inline val config
            get() = ConfigManager.config.chatBridge
    }
}
