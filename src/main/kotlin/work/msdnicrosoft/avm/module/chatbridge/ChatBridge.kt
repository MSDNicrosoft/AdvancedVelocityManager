package work.msdnicrosoft.avm.module.chatbridge

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import net.kyori.adventure.text.Component
import taboolib.common.platform.command.CommandHeader
import taboolib.common5.util.startsWithAny
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.command.chatbridge.MsgCommand
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.Logging
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

object ChatBridge {

    private inline val config
        get() = ConfigManager.config.chatBridge

    private val PRIVATE_CHAT_COMMANDS by lazy {
        MsgCommand.javaClass.getAnnotation(CommandHeader::class.java).aliases
    }

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
         * If they match one of the patterns,
         * chat messages will be sent to the backend server.
         */
        PATTERN;

        companion object {
            fun of(mode: String): PassthroughMode = valueOf(mode.uppercase())
        }
    }

    fun init() {
        plugin.server.eventManager.register(plugin, this)
    }

    fun disable() {
        plugin.server.eventManager.unregisterListener(plugin, this)
    }

    /**
     * Represents the current passthrough mode for chat messages.
     *
     * @property mode The current passthrough mode. Defaults to [PassthroughMode.ALL].
     */
    var mode: PassthroughMode = PassthroughMode.ALL

    @Suppress("Deprecation")
    @Subscribe(order = PostOrder.FIRST)
    fun onPlayerChatChat(event: PlayerChatEvent) {
        if (!config.enabled) return

        val message = ChatMessage(event, config).build()
        val serverName = event.player.currentServer.get().serverInfo.name

        when (mode) {
            PassthroughMode.ALL -> sendMessage(message, serverName)
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
                if (matched) {
                    sendMessage(message, serverName)
                } else {
                    event.result = PlayerChatEvent.ChatResult.denied()
                    sendMessage(message)
                }
            }
        }

        if (config.logging) {
            Logging.log(
                "[$serverName]<${event.player.username}> ${event.message}"
            )
        }
    }

    /**
     * This event listener is triggered when a command is executed.
     * It checks if the command is related to private chat
     * and if the configuration does not allow for taking over private chat.
     *
     * If the conditions are met, the command is forwarded to the server.
     */
    @Subscribe
    fun onCommandExecute(event: CommandExecuteEvent) {
        val isPrivateChat = PRIVATE_CHAT_COMMANDS.any { event.command.split(" ")[0].startsWithAny(event.command) }

        if (!isPrivateChat) return

        if (!config.takeOverPrivateChat) {
            event.result = CommandExecuteEvent.CommandResult.forwardToServer()
        }
    }

    private fun sendMessage(message: Component, vararg ignoredServer: String) {
        plugin.server.allServers
            .parallelStream()
            .filter { it.serverInfo.name !in ignoredServer }
            .forEach { server ->
                server.playersConnected.forEach { player ->
                    player.sendMessage(message)
                }
            }
    }
}
