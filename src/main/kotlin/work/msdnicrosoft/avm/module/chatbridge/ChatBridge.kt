package work.msdnicrosoft.avm.module.chatbridge

import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import net.kyori.adventure.text.Component
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common5.util.startsWithAny
import work.msdnicrosoft.avm.command.chatbridge.MsgCommand
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

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

    private val config
        get() = ConfigManager.config.chatBridge

    @Suppress("unused")
    @SubscribeEvent(postOrder = PostOrder.FIRST)
    fun onPlayerChatChat(event: PlayerChatEvent) {
        if (!config.enabled) return

        val message = ChatMessage(event, config).build()
        val currentServerName = event.player.currentServer.get().serverInfo.name

        when (mode) {
            PassthroughMode.ALL -> sendMessage(message, currentServerName)
            PassthroughMode.NONE -> {
                event.result = PlayerChatEvent.ChatResult.denied()
                sendMessage(message)
            }

            PassthroughMode.PATTERN -> {
                val playerMessage = event.message
                val patterns = config.chatPassthrough.pattern
                val matched = patterns.contains.any { it in playerMessage } ||
                    patterns.startswith.any { playerMessage.startsWith(it) } ||
                    patterns.endswith.any { playerMessage.endsWith(it) }
                if (!matched) {
                    event.result = PlayerChatEvent.ChatResult.denied()
                    sendMessage(message)
                } else {
                    sendMessage(message, currentServerName)
                }
            }
        }
    }

    /**
     * This event listener is triggered when a command is executed.
     * It checks if the command is related to private chat
     * and if the configuration not allows for taking over private chat.
     *
     * If the conditions are met, the command is forwarded to the server.
     */
    @Suppress("unused")
    @SubscribeEvent
    fun onCommandExecute(event: CommandExecuteEvent) {
        val isPrivateChat = MsgCommand.javaClass.getAnnotation(CommandHeader::class.java).aliases.any {
            event.command.split(" ")[0].startsWithAny(event.command)
        }

        if (!isPrivateChat) return

        if (!config.takeOverPrivateChat) {
            event.result = CommandExecuteEvent.CommandResult.forwardToServer()
        }
    }

    private fun sendMessage(message: Component, vararg ignoredServer: String) {
        for (server in AVM.plugin.server.allServers) {
            if (server.serverInfo.name in ignoredServer) continue
            server.playersConnected.forEach { player ->
                player.sendMessage(message)
            }
        }
    }
}
