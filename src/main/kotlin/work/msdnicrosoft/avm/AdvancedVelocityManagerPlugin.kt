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
import work.msdnicrosoft.avm.module.chatbridge.inject.InstrumentationAccess
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.CommandSessionManager

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
        ConfigManager.load()
        Language.default = ConfigManager.config.defaultLang
        CommandSessionManager.onEnable()
        PlayerCache.onEnable()
        WhitelistManager.onEnable()
    }

    override fun onDisable() {
        WhitelistManager.onDisable()
        CommandSessionManager.onDisable()

        plugin.server.commandManager.run {
            unregister("avm")
            unregister("avmwl")
            unregister("msg")
        }
        plugin.server.eventManager.unregisterListeners(plugin)
    }

    override fun onActive() {
        if (self.version.get().contains("DEV")) {
            logger.warn("You are using the development version of this plugin.")
        }
        if (self.version.get().contains("SNAPSHOT")) {
            logger.warn("You are using the snapshot version of this plugin.")
        }
    }

    fun reload(): Boolean {
        try {
            if (!ConfigManager.reload()) {
                return false
            }

            CommandSessionManager.onEnable()
            PlayerCache.reload()
            WhitelistManager.onEnable(reload = true)
            reloadLanguage()
            return true
        } catch (e: Exception) {
            logger.error("Failed to reload plugin", e)
            return false
        }
    }

    private fun reloadLanguage() {
        logger.info("Reloading language...")
        Language.reload()
        Language.default = ConfigManager.config.defaultLang
    }
}
