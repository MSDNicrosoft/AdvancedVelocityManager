package work.msdnicrosoft.avm

import com.velocitypowered.api.plugin.PluginDescription
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.common.util.unsafeLazy
import taboolib.module.lang.Language
import taboolib.platform.VelocityPlugin
import work.msdnicrosoft.avm.config.AVMConfig
import work.msdnicrosoft.avm.impl.VelocityAdapter
import work.msdnicrosoft.avm.module.chatbridge.ChatBridge
import work.msdnicrosoft.avm.module.chatbridge.ChatBridge.PassthroughMode
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.FileUtil.decodeFromString
import work.msdnicrosoft.avm.util.FileUtil.encodeToString
import work.msdnicrosoft.avm.util.FileUtil.readTextWithBuffer
import work.msdnicrosoft.avm.util.FileUtil.writeTextWithBuffer
import work.msdnicrosoft.avm.util.FileUtil.yaml
import work.msdnicrosoft.avm.util.command.CommandSessionManager

@PlatformSide(Platform.VELOCITY)
object AdvancedVelocityManagerPlugin : Plugin() {

    val plugin by unsafeLazy { VelocityPlugin.getInstance() }

    val logger = ComponentLogger.logger("advancedvelocitymanager")

    val self: PluginDescription
        get() = plugin.server.pluginManager.getPlugin("advancedvelocitymanager").get().description

    val configFile by unsafeLazy { getDataFolder().resolve("config.yml") }

    val configLock = Object()

    lateinit var config: AVMConfig

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
        loadConfig()
        Language.default = config.defaultLang
        CommandSessionManager.onEnable()
        PlayerCache.onEnable()
        WhitelistManager.onEnable()
    }

    override fun onDisable() {
        WhitelistManager.onDisable()
        CommandSessionManager.onDisable()
    }

    private fun loadConfig(reload: Boolean = false) {
        if (configFile.exists()) {
            try {
                info("${if (reload) "Reloading" else "Loading"} config...")
                withLock { config = yaml.decodeFromString<AVMConfig>(configFile.readTextWithBuffer()) }
                // TODO Migrate config
                checkConfig()
            } catch (e: Exception) {
                error("Failed to load config: ${e.message}")
            }
        } else {
            try {
                info("Config file does not exist, generating default config...")
                configFile.parentFile.mkdirs()
                withLock {
                    config = AVMConfig()
                    configFile.writeTextWithBuffer(yaml.encodeToString(config))
                }
                saveConfig()
            } catch (e: Exception) {
                error("Failed to initialize config: ${e.message}")
            }
        }
    }

    fun saveConfig() = withLock {
        runCatching {
            configFile.writeTextWithBuffer(yaml.encodeToString(config))
        }.onFailure {
            error("Failed to save config: ${it.message}")
        }.isSuccess
    }

    private fun checkConfig() {
        try {
            ChatBridge.mode = PassthroughMode.valueOf(config.chatBridge.chatPassthrough.mode.uppercase())
        } catch (_: IllegalArgumentException) {
            warning("Incorrect Chat-Passthrough mode name!")
            warning("Plugin will fallback to `ALL` mode")
            ChatBridge.mode = PassthroughMode.ALL
        }
    }

    fun <T> withLock(block: () -> T) = synchronized(configLock) { block() }

    fun reload() = runCatching {
        loadConfig(reload = true)

        info("Reloading language...")
        Language.reload()
        Language.default = config.defaultLang

        CommandSessionManager.onEnable()
        PlayerCache.onEnable()
        WhitelistManager.onEnable(reload = true)
    }.isSuccess
}
