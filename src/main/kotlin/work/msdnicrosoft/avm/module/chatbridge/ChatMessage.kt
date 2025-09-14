package work.msdnicrosoft.avm.module.chatbridge

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ServerConnection
import com.velocitypowered.api.proxy.server.ServerPing
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.DateTimeUtil.getDateTime
import work.msdnicrosoft.avm.util.component.ComponentSerializer.STYLE_ONLY_MINI_MESSAGE
import work.msdnicrosoft.avm.util.component.ComponentUtil.createClickEvent
import work.msdnicrosoft.avm.util.component.hoverText
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.TIMEOUT_PING_RESULT
import work.msdnicrosoft.avm.util.server.nickname
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
    private val server: ServerConnection = player.currentServer.get()
    private val serverName: String = server.serverInfo.name
    private val serverNickname: String = server.serverInfo.nickname
    private val serverOnlinePlayers: String = server.server.playersConnected.size.toString()

    private val playerUsername: String = player.username
    private val playerUuid: String = player.uniqueId.toString()
    private val playerPing: String = player.ping.toString()

    private val serverPing: ServerPing by lazy {
        try {
            server.server.ping().get(20, TimeUnit.MILLISECONDS)
        } catch (_: TimeoutException) {
            TIMEOUT_PING_RESULT
        }
    }
    private val serverVersion: String by lazy {
        serverPing.version.name
    }

    /**
     * Build the final chat message with all specified formats and events.
     * @return The built chat message with all formats and events applied.
     */
    fun build(): Component = Component.join(
        JoinConfiguration.noSeparators(),
        config.publicChatFormat.map { format ->
            format.text.deserialize()
                .hoverText(format.hover?.joinToString("\n")?.deserialize())
                .clickEvent(createClickEvent(format) { replacePlaceholders() })
        }
    )

    /**
     * Deserialize the message by replacing placeholders with actual values.
     * @return The deserialized message with placeholders replaced.
     */
    private fun String.deserialize(): Component =
        if ("<server_version>" in this) {
            STYLE_ONLY_MINI_MESSAGE.deserialize(
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
        } else {
            STYLE_ONLY_MINI_MESSAGE.deserialize(
                this,
                Placeholder.unparsed("player_name", playerUsername),
                Placeholder.unparsed("player_uuid", playerUuid),
                Placeholder.unparsed("player_ping", playerPing),
                Placeholder.unparsed("server_name", serverName),
                Placeholder.unparsed("server_nickname", serverNickname),
                Placeholder.unparsed("server_online_players", serverOnlinePlayers),
                Placeholder.unparsed("player_message_sent_time", getDateTime()),
                if (config.allowFormatCode) {
                    Placeholder.parsed("player_message", message)
                } else {
                    Placeholder.unparsed("player_message", message)
                },
            )
        }

    private fun String.replacePlaceholders(): String =
        this.replace("<player_name>", playerUsername)
            .replace("<player_uuid>", playerUuid)
            .replace("<player_ping>", playerPing)
            .replace("<player_message>", message)
            .replace("<server_name>", serverName)
            .replace("<server_nickname>", serverNickname)
            .replace("<server_online_players>", serverOnlinePlayers)
            .let { if ("<server_version>" in this) it.replace("<server_version>", serverVersion) else it }

    companion object {
        private inline val config get() = ConfigManager.config.chatBridge
    }
}
