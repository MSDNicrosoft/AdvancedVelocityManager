package work.msdnicrosoft.avm

import com.google.inject.Inject
import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.PluginDescription
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.ChannelRegistrar
import com.velocitypowered.api.scheduler.Scheduler
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import work.msdnicrosoft.avm.command.AVMCommand
import work.msdnicrosoft.avm.command.WhitelistCommand
import work.msdnicrosoft.avm.command.chatbridge.MsgCommand
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.i18n.TranslateManager
import work.msdnicrosoft.avm.module.EventBroadcast
import work.msdnicrosoft.avm.module.Logging
import work.msdnicrosoft.avm.module.TabSyncHandler
import work.msdnicrosoft.avm.module.chatbridge.ChatBridge
import work.msdnicrosoft.avm.module.command.session.CommandSessionManager
import work.msdnicrosoft.avm.module.mapsync.WorldInfoHandler
import work.msdnicrosoft.avm.module.mapsync.XaeroMapHandler
import work.msdnicrosoft.avm.module.packet.PacketHandler
import work.msdnicrosoft.avm.module.reconnect.ReconnectHandler
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.patch.Patch
import java.nio.file.Path

class AdvancedVelocityManagerPlugin {
    val server: ProxyServer
    val dataDirectory: Path
    val self: PluginDescription by lazy {
        this.server.pluginManager.getPlugin("advancedvelocitymanager").get().description
    }

    @Inject
    constructor(server: ProxyServer, @DataDirectory dataDirectory: Path) {
        this.server = server
        this.dataDirectory = dataDirectory
        plugin = this
    }

    @Suppress("unused")
    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        Patch.init()

        logger.debug("Nya~!")

        require(ConfigManager.load()) { "Failed to load configuration, aborting initialization..." }

        TranslateManager.init()
        this.initializeModules()
        this.registerCommands()

        this.self.version.get().let { version ->
            if (version.contains("DEV")) logger.warn("You are using the development version of this plugin.")
            if (version.contains("SNAPSHOT")) logger.warn("You are using the snapshot version of this plugin.")
        }
    }

    @Suppress("unused")
    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        this.disableModules()
        this.unregisterCommands()
    }

    private fun initializeModules() {
        PacketHandler.init()
        CommandSessionManager.init()
        WhitelistManager.init()
        ChatBridge.init()
        TabSyncHandler.init()
        EventBroadcast.init()
        WorldInfoHandler.init()
        XaeroMapHandler.init()
        ReconnectHandler.init()
        Logging.init()
    }

    private fun disableModules() {
        PacketHandler.disable()
        WhitelistManager.disable()
        ChatBridge.disable()
        TabSyncHandler.disable()
        EventBroadcast.disable()
        WorldInfoHandler.disable()
        XaeroMapHandler.disable()
        ReconnectHandler.disable()
        CommandSessionManager.disable()
        TranslateManager.disable()
    }

    private fun registerCommands() {
        MsgCommand.init()
        AVMCommand.init()
        WhitelistCommand.init()
    }

    private fun unregisterCommands() {
        MsgCommand.disable()
        AVMCommand.disable()
        WhitelistCommand.disable()
    }

    @Suppress("TooGenericExceptionCaught")
    fun reload(): Boolean {
        try {
            if (!ConfigManager.reload()) {
                return false
            }

            TranslateManager.reload()
            CommandSessionManager.reload()
            PlayerCache.reload()
            WhitelistManager.reload()
            return true
        } catch (e: Exception) {
            logger.error("Failed to reload plugin", e)
            return false
        }
    }

    companion object {
        lateinit var plugin: AdvancedVelocityManagerPlugin
            private set

        val logger: ComponentLogger = ComponentLogger.logger("AdvancedVelocityManager")
        inline val server: ProxyServer get() = this.plugin.server
        inline val scheduler: Scheduler get() = this.server.scheduler
        inline val dataDirectory: Path get() = this.plugin.dataDirectory
        inline val commandManager: CommandManager get() = this.server.commandManager
        inline val eventManager: EventManager get() = this.server.eventManager
        inline val channelRegistrar: ChannelRegistrar get() = this.server.channelRegistrar
    }
}
