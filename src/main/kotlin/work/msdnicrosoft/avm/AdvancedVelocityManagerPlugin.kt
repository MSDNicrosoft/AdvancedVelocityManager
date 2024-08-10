package work.msdnicrosoft.avm

import com.velocitypowered.api.plugin.PluginDescription
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
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

    val logger = ComponentLogger.logger("advancedvelocitymanager")

    val self: PluginDescription
        get() = plugin.server.pluginManager.getPlugin("advancedvelocitymanager").get().description

    var hasFloodgate: Boolean = false

    override fun onLoad() {
        info("Detected dynamic java agent loading warnings.")
        info("It is expected behavior and you can safely ignore the warnings.")
        val adapter = VelocityAdapter()
        val adapterKey = PlatformFactory.serviceMap.keys.first { "PlatformAdapter" in it }
        PlatformFactory.serviceMap[adapterKey] = adapter
    }

    override fun onEnable() {
        hasFloodgate = plugin.server.pluginManager.getPlugin("floodgate") != null
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
    }

    fun reload() = runCatching {
        ConfigManager.reload()

        info("Reloading language...")
        Language.reload()
        Language.default = ConfigManager.config.defaultLang

        CommandSessionManager.onEnable()
        PlayerCache.onEnable()
        WhitelistManager.onEnable(reload = true)
    }.isSuccess
}
