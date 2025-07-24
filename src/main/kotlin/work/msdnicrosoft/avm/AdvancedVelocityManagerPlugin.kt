package work.msdnicrosoft.avm

import com.velocitypowered.api.plugin.PluginDescription
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.util.unsafeLazy
import taboolib.module.lang.Language
import taboolib.platform.VelocityPlugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.CommandSessionManager
import work.msdnicrosoft.avm.module.EventBroadcast
import work.msdnicrosoft.avm.module.TabSyncHandler
import work.msdnicrosoft.avm.module.chatbridge.ChatBridge
import work.msdnicrosoft.avm.module.mapsync.WorldInfoHandler
import work.msdnicrosoft.avm.module.mapsync.XaeroMapHandler
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.patch.InstrumentationAccess

@PlatformSide(Platform.VELOCITY)
object AdvancedVelocityManagerPlugin : Plugin() {

    val plugin by unsafeLazy { VelocityPlugin.getInstance() }

    val logger = ComponentLogger.logger("AdvancedVelocityManager")

    val self: PluginDescription by lazy {
        plugin.server.pluginManager.getPlugin("advancedvelocitymanager").get().description
    }

    override fun onLoad() {
        InstrumentationAccess.init()
    }

    override fun onEnable() {
        logger.debug("Nya~!")

        require(ConfigManager.load()) { "Failed to load configuration, aborting initialization" }

        loadLanguage()

        CommandSessionManager.init()
        WhitelistManager.init()
        ChatBridge.init()
        TabSyncHandler.init()
        EventBroadcast.init()
        WorldInfoHandler.init()
        XaeroMapHandler.init()
        Metrics.init()
    }

    override fun onDisable() {
        WhitelistManager.disable()
        ChatBridge.disable()
        TabSyncHandler.disable()
        EventBroadcast.disable()
        WorldInfoHandler.disable()
        CommandSessionManager.disable()
        plugin.server.commandManager.run {
            unregister("avm")
            unregister("avmwl")
            unregister("msg")
        }
    }

    override fun onActive() {
        self.version.get().let { version ->
            if (version.contains("DEV")) logger.warn("You are using the development version of this plugin.")
            if (version.contains("SNAPSHOT")) logger.warn("You are using the snapshot version of this plugin.")
        }
    }

    fun reload(): Boolean {
        try {
            if (!ConfigManager.reload()) {
                return false
            }

            loadLanguage(reload = true)
            CommandSessionManager.reload()
            PlayerCache.reload()
            WhitelistManager.reload()
            return true
        } catch (e: Exception) {
            logger.error("Failed to reload plugin", e)
            return false
        }
    }

    private fun loadLanguage(reload: Boolean = false) {
        if (reload) {
            logger.info("Reloading language...")
            Language.reload()
        } else {
            logger.info("Loading language...")
        }
        Language.default = ConfigManager.config.defaultLang
    }
}
