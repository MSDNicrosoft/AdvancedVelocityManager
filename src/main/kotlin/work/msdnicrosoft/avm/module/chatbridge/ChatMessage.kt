package work.msdnicrosoft.avm.module.chatbridge

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ServerConnection
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.DateTimeUtil.getDateTime
import work.msdnicrosoft.avm.util.component.ComponentSerializer
import work.msdnicrosoft.avm.util.component.builder.minimessage.miniMessage
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.placeholders
import work.msdnicrosoft.avm.util.component.builder.style.styled
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.TIMEOUT_PING_RESULT
import work.msdnicrosoft.avm.util.server.nickname
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class ChatMessage(private val player: Player, private val message: String) {
    private val serverConnection: ServerConnection = player.currentServer.get()
    private val serverVersion: String by lazy {
        try {
            player.currentServer.get().server.ping().get(20, TimeUnit.MILLISECONDS)
        } catch (_: TimeoutException) {
            TIMEOUT_PING_RESULT
        }.version.name
    }

    private val basicPlaceHolders: List<TagResolver> = placeholders {
        unparsed("player_name", player.username)
        unparsed("player_uuid", player.uniqueId.toString())
        numeric("player_ping", player.ping)
        unparsed("server_name", serverConnection.serverInfo.name)
        component("server_nickname", serverConnection.serverInfo.nickname)
        numeric("server_online_players", serverConnection.server.playersConnected.size)
        unparsed("player_message_sent_time", getDateTime())
        if (config.allowFormatCode) {
            parsed("player_message", message)
        } else {
            unparsed("player_message", message)
        }
    }

    fun toComponent(): Component = Component.join(
        JoinConfiguration.noSeparators(),
        config.publicChatFormat.map { format ->
            val tagResolvers: List<TagResolver> = buildList {
                addAll(basicPlaceHolders)
                if ("<server_version>" in format.text) {
                    add(Placeholder.unparsed("server_version", serverVersion))
                }
            }
            miniMessage(format.text, provider = ComponentSerializer.STYLE_ONLY_MINI_MESSAGE) {
                placeholders { tagResolvers(tagResolvers) }
            } styled {
                hoverText {
                    miniMessage(format.hover?.joinToString("\n").orEmpty()) {
                        placeholders { tagResolvers(tagResolvers) }
                    }
                }
                click(format.applyReplace { replacePlaceholders() })
            }
        }
    )

    private fun String.replacePlaceholders(): String = this
        .replace("<player_name>", player.username)
        .replace("<player_uuid>", player.uniqueId.toString())
        .replace("<player_ping>", player.ping.toString())
        .replace("<player_message>", message)
        .replace("<server_name>", serverConnection.serverInfo.name)
        .replace(
            "<server_nickname>",
            ComponentSerializer.BASIC_PLAIN_TEXT.serialize(serverConnection.serverInfo.nickname)
        )
        .replace("<server_online_players>", serverConnection.server.playersConnected.size.toString())
        .let { if ("<server_version>" in this) it.replace("<server_version>", serverVersion) else it }

    companion object {
        private inline val config get() = ConfigManager.config.chatBridge
    }
}
