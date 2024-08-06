package work.msdnicrosoft.avm.module.chatbridge

import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.server.ServerPing
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import work.msdnicrosoft.avm.config.AVMConfig
import work.msdnicrosoft.avm.util.ComponentUtil.createClickEvent
import work.msdnicrosoft.avm.util.ComponentUtil.createHoverEvent
import work.msdnicrosoft.avm.util.ComponentUtil.serializer
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.DateTimeUtil.getDateTime
import work.msdnicrosoft.avm.util.StringUtil.replace
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Represents a chat message with various placeholders to be replaced.
 * @property event The [PlayerChatEvent] associated with the message.
 */
class ChatMessage(val event: PlayerChatEvent, private val config: AVMConfig.ChatBridge) {

    private val server = event.player.currentServer.get()
    private val serverPing = try {
        server.server.ping().get(20, TimeUnit.MILLISECONDS)
    } catch (_: TimeoutException) {
        ServerPing.builder()
            .version(ServerPing.Version(-1, "Unknown"))
            .description(Component.text("Unknown"))
            .build()
    }
    private val serverName = server.serverInfo.name
    private val serverNickname = getServerNickname(serverName)
    private val serverOnlinePlayers = server.server.playersConnected.size.toString()
    private val serverVersion = serverPing.version.name

    private val player = event.player
    private val playerUsername = player.username
    private val playerUuid = player.uniqueId.toString()
    private val playerPing = player.ping.let { if (it == -1L) "Unknown" else it.toString() }

    /**
     * Deserialize the message by replacing placeholders with actual values.
     * @return The deserialized message with placeholders replaced.
     */
    private fun String.deserialize() = serializer.buildComponent(this)
        .replace("%player_name%", playerUsername)
        .replace("%player_uuid%", playerUuid)
        .replace("%player_ping%", playerPing)
        .replace("%player_message%", event.message.let { if (config.allowFormatCode) serializer.parse(it) else it })
        .replace("%server_name%", serverName)
        .replace("%server_nickname%", serverNickname)
        .replace("%server_online_players%", serverOnlinePlayers)
        .replace("%server_version%", serverVersion)
        .replace("%player_message_sent_time%", getDateTime())
        .build()

    /**
     * Replace placeholders in the message with actual player and server information.
     * @return The message with placeholders replaced.
     */
    private fun String.replacePlaceholders() = this.replace(
        "%player_name%" to playerUsername,
        "%player_uuid%" to playerUuid,
        "%player_ping%" to playerPing,
        "%player_message%" to event.message,
        "%server_name%" to serverName,
        "%server_nickname%" to serverNickname,
        "%server_online_players%" to serverOnlinePlayers,
        "%server_version%" to serverVersion
    )

    /**
     * Build the final chat message with all specified formats and events.
     * @return The built chat message with all formats and events applied.
     */
    fun build() = Component.join(
        JoinConfiguration.noSeparators(),
        config.publicChatFormat.map { format ->
            format.text.deserialize()
                .clickEvent(createClickEvent(format) { replacePlaceholders() })
                .hoverEvent(createHoverEvent(format) { deserialize() })
        }
    )
}
