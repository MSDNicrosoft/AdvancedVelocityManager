package work.msdnicrosoft.avm.module.chatbridge

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import net.kyori.adventure.text.Component
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.command.chatbridge.MsgCommand
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.Logging

object ChatBridge {
    private inline val config get() = ConfigManager.config.chatBridge

    /**
     * The current passthrough mode for chat messages.
     * Defaults to [PassthroughMode.ALL].
     */
    var mode: PassthroughMode = PassthroughMode.ALL

    fun init() {
        plugin.server.eventManager.register(plugin, this)
    }

    fun disable() {
        plugin.server.eventManager.unregisterListener(plugin, this)
    }

    @Suppress("Deprecation")
    @Subscribe(order = PostOrder.FIRST)
    fun onPlayerChatChat(event: PlayerChatEvent) {
        if (!config.enabled) {
            return
        }

        val message: Component = ChatMessage(event.player, event.message).toComponent()
        val serverName: String = event.player.currentServer.get().serverInfo.name

        when (this.mode) {
            PassthroughMode.ALL -> this.sendMessage(message, serverName)
            PassthroughMode.NONE -> {
                event.result = PlayerChatEvent.ChatResult.denied()
                this.sendMessage(message)
            }

            PassthroughMode.PATTERN -> {
                val playerMessage = event.message
                val patterns = config.chatPassthrough.pattern
                val matched = patterns.contains.any { it in playerMessage } ||
                    patterns.startswith.any { playerMessage.startsWith(it) } ||
                    patterns.endswith.any { playerMessage.endsWith(it) }
                if (matched) {
                    this.sendMessage(message, serverName)
                } else {
                    event.result = PlayerChatEvent.ChatResult.denied()
                    this.sendMessage(message)
                }
            }
        }

        if (config.logging) {
            Logging.log("[$serverName]<${event.player.username}> ${event.message}")
        }
    }

    @Subscribe
    fun onCommandExecute(event: CommandExecuteEvent) {
        val eventCommand: String = event.command
            .split(" ")
            .first()
            .replace("/", "")

        // Check if the executed command is related to private chat
        if (!MsgCommand.aliases.any { eventCommand.startsWith(it) }) {
            return
        }

        if (!config.takeOverPrivateChat) {
            event.result = CommandExecuteEvent.CommandResult.forwardToServer()
        }
    }

    private fun sendMessage(message: Component, vararg ignoredServer: String) {
        plugin.server.allServers.parallelStream()
            .filter { server -> server.serverInfo.name !in ignoredServer }
            .forEach { server ->
                server.playersConnected.forEach { player ->
                    player.sendMessage(message)
                }
            }
    }
}
