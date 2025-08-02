package work.msdnicrosoft.avm.module.chatbridge

import com.velocitypowered.api.event.player.PlayerChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import work.msdnicrosoft.avm.config.data.ChatBridge
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.DateTimeUtil.getDateTime
import work.msdnicrosoft.avm.util.component.ComponentUtil.createClickEvent
import work.msdnicrosoft.avm.util.component.ComponentUtil.styleOnlyMiniMessage
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.TIMEOUT_PING_RESULT
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Represents a chat message sent by a player. This class is used to format
 * the message based on the configuration specified in the `chat-bridge` section
 * of the main configuration file.
 *
 * @param event The event that triggered the formatting of the message.
 * @param config The configuration that specifies the formats and events for the message.
 */
class ChatMessage(private val event: PlayerChatEvent, private val config: ChatBridge) {

    private val server = event.player.currentServer.get()
    private val serverPing = try {
        server.server.ping().get(20, TimeUnit.MILLISECONDS)
    } catch (_: TimeoutException) {
        TIMEOUT_PING_RESULT
    }
    private val serverName = server.serverInfo.name
    private val serverNickname = getServerNickname(serverName)
    private val serverOnlinePlayers = server.server.playersConnected.size.toString()
    private val serverVersion = serverPing.version.name

    private val player = event.player
    private val playerUsername = player.username
    private val playerUuid = player.uniqueId.toString()
    private val playerPing = player.ping.toString()

    /**
     * Deserialize the message by replacing placeholders with actual values.
     * @return The deserialized message with placeholders replaced.
     */
    private fun String.deserialize() = styleOnlyMiniMessage.deserialize(
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
            Placeholder.parsed("player_message", event.message)
        } else {
            Placeholder.unparsed("player_message", event.message)
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
            .replace("<player_message>", event.message)
            .replace("<server_name>", serverName)
            .replace("<server_nickname>", serverNickname)
            .replace("<server_online_players>", serverOnlinePlayers)
            .replace("<server_version>", serverVersion)

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
}
