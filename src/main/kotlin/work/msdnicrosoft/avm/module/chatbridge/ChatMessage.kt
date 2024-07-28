package work.msdnicrosoft.avm.module.chatbridge

import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.server.ServerPing
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import taboolib.common.platform.function.warning
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.config
import work.msdnicrosoft.avm.config.AVMConfig
import work.msdnicrosoft.avm.util.ComponentUtil.serializer
import work.msdnicrosoft.avm.util.ConfigUtil
import work.msdnicrosoft.avm.util.Extensions.replace
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Represents a chat message with various placeholders to be replaced.
 * @property event The [PlayerChatEvent] associated with the message.
 */
class ChatMessage(val event: PlayerChatEvent) {

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
    private val serverNickname = ConfigUtil.getServerNickname(serverName)
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
        .replace(
            "%player_message%",
            event.message.let { if (config.chatBridge.allowFormatCode) serializer.parse(it) else it }
        )
        .replace("%server_name%", serverName)
        .replace("%server_nickname%", serverNickname)
        .replace("%server_online_players%", serverOnlinePlayers)
        .replace("%server_version%", serverVersion)
        .replace(
            "%player_message_sent_time%",
            LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
        .build()

    /**
     * Replace placeholders in the message with actual player and server information.
     * @return The message with placeholders replaced.
     */
    private fun String.replacePlaceholders() = this.replace(
        "%player_name%" to event.player.username,
        "%player_uuid%" to playerUuid,
        "%player_ping%" to playerPing,
        "%player_message%" to event.message,
        "%server_name%" to serverName,
        "%server_nickname%" to serverNickname,
        "%server_online_players%" to serverOnlinePlayers,
        "%server_version%" to serverVersion
    )

    /**
     * Create a [ClickEvent] based on the provided format.
     * @param format The format to create the [ClickEvent] from.
     * @return The [ClickEvent] created based on the format.
     */
    private fun createClickEvent(format: AVMConfig.ChatBridge.Format): ClickEvent? {
        validateFormat(format)
        return when {
            !format.command.isNullOrEmpty() -> ClickEvent.runCommand(format.command.replacePlaceholders())
            !format.suggest.isNullOrEmpty() -> ClickEvent.suggestCommand(format.suggest.replacePlaceholders())
            !format.url.isNullOrEmpty() -> ClickEvent.openUrl(format.url.replacePlaceholders())
            !format.clipboard.isNullOrEmpty() -> ClickEvent.copyToClipboard(format.clipboard.replacePlaceholders())
            else -> null
        }
    }

    /**
     * Create a [HoverEvent] based on the provided format.
     * @param format The format to create the [HoverEvent] from.
     * @return The [HoverEvent] created based on the format.
     */
    private fun createHoverEvent(format: AVMConfig.ChatBridge.Format): HoverEvent<Component?>? =
        if (!format.hover.isNullOrEmpty()) {
            HoverEvent.showText(format.hover.joinToString("\n").deserialize())
        } else {
            null
        }

    /**
     * Build the final chat message with all specified formats and events.
     * @return The built chat message with all formats and events applied.
     */
    fun build() = Component.join(
        JoinConfiguration.noSeparators(),
        buildList<Component> {
            config.chatBridge.chatFormat.forEach { format ->
                add(
                    format.text.deserialize().let { baseComponent ->
                        var component = baseComponent
                        component = component.clickEvent(createClickEvent(format))

                        if (!format.hover.isNullOrEmpty()) {
                            component = component.hoverEvent(createHoverEvent(format))
                        }
                        component
                    }
                )
            }
        }
    )

    /**
     * Validates a chat bridge format to ensure that exactly one of the provided options is provided and non-empty.
     *
     * @param format the format to validate
     * @throws IllegalArgumentException if more than one option is provided or if an option is empty
     */
    private fun validateFormat(format: AVMConfig.ChatBridge.Format) {
        val conflicted = listOf(
            !format.command.isNullOrEmpty(),
            !format.suggest.isNullOrEmpty(),
            !format.url.isNullOrEmpty(),
            !format.clipboard.isNullOrEmpty(),
        ).count { it } > 1
        if (conflicted) {
            warning("Exactly one of 'command', 'suggest', 'url', or 'clipboard' should be provided and non-empty.")
        }
    }
}
