package work.msdnicrosoft.avm

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.util.unsafeLazy
import taboolib.module.lang.Language
import taboolib.platform.VelocityPlugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.impl.VelocityAdapter
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.CommandSessionManager

@PlatformSide(Platform.VELOCITY)
object AdvancedVelocityManagerPlugin : Plugin() {

    val plugin by unsafeLazy { VelocityPlugin.getInstance() }

    val logger = ComponentLogger.logger("AdvancedVelocityManager")

    val self
        get() = plugin.server.pluginManager.getPlugin("advancedvelocitymanager").get().description

    override fun onLoad() {
        logger.info("Detected dynamic java agent loading warnings.")
        logger.info("It is expected behavior and you can safely ignore the warnings.")
        val adapter = VelocityAdapter()
        val adapterKey = PlatformFactory.serviceMap.keys.first { "PlatformAdapter" in it }
        PlatformFactory.serviceMap[adapterKey] = adapter
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

    fun reload(): Boolean {
        try {
            if (!ConfigManager.reload()) {
                return false
            }

            logger.info("Reloading language...")
            Language.reload()
            Language.default = ConfigManager.config.defaultLang

            CommandSessionManager.onEnable()
            PlayerCache.onEnable()
            WhitelistManager.onEnable(reload = true)
            return true
        } catch (e: Exception) {
            logger.error("Failed to reload plugin", e)
            return false
        }
    }
}
