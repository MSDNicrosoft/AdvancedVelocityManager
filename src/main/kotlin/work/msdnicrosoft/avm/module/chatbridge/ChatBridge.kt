package work.msdnicrosoft.avm.module.chatbridge

import com.velocitypowered.api.event.player.PlayerChatEvent
import net.kyori.adventure.text.Component
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.SubscribeEvent
import kotlin.collections.any
import kotlin.collections.contains
import kotlin.collections.forEach
import kotlin.text.contains
import kotlin.text.endsWith
import kotlin.text.startsWith
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@Suppress("Indentation")
object ChatBridge {

    /**
     * Represents the different modes of passthrough for chat messages.
     */
    enum class PassthroughMode {
        /**
         * All chat messages will be sent to the backend server.
         */
        ALL,

        /**
         * No chat messages will be sent to the backend server.
         */
        NONE,

        /**
         * If they match one of patterns,
         * chat messages will be sent to the backend server.
         */
        PATTERN
    }

    /**
     * Represents the current passthrough mode for chat messages.
     *
     * @property mode The current passthrough mode. Defaults to [PassthroughMode.ALL].
     */
    var mode: PassthroughMode = PassthroughMode.ALL

    @SubscribeEvent(postOrder = PostOrder.FIRST)
    fun onPlayerChatChat(event: PlayerChatEvent) {
        val config = AVM.config.chatBridge

        if (!config.enabled) return

        val formattedMessage = ChatMessage(event).build()

        when (mode) {
            PassthroughMode.ALL -> {}
            PassthroughMode.NONE -> {
                event.result = PlayerChatEvent.ChatResult.denied()
                sendMessage(formattedMessage)
            }

            PassthroughMode.PATTERN -> {
                val playerMessage = event.message
                val patterns = config.chatPassthrough.pattern
                val matched = patterns.contains.any { it in playerMessage } ||
                    patterns.startswith.any { playerMessage.startsWith(it) } ||
                    patterns.endswith.any { playerMessage.endsWith(it) }
                if (!matched) {
                    event.result = PlayerChatEvent.ChatResult.denied()
                    sendMessage(formattedMessage)
                }
            }
        }
    }

    private fun sendMessage(message: Component, vararg ignoredServers: String) {
        for (server in AVM.plugin.server.allServers) {
            if (server.serverInfo.name in ignoredServers) continue

            server.playersConnected.forEach { player ->
                player.sendMessage(message)
            }
        }
    }
}
