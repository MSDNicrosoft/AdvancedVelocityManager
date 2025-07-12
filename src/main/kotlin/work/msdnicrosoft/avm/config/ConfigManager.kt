package work.msdnicrosoft.avm.config

import com.charleskorn.kaml.YamlException
import kotlinx.serialization.SerializationException
import taboolib.common.platform.function.getDataFolder
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.logger
import work.msdnicrosoft.avm.module.chatbridge.ChatBridge
import work.msdnicrosoft.avm.util.file.FileUtil.YAML
import work.msdnicrosoft.avm.util.file.decodeFromString
import work.msdnicrosoft.avm.util.file.encodeToString
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import work.msdnicrosoft.avm.util.file.writeTextWithBuffer
import work.msdnicrosoft.avm.util.string.isValidUrl
import java.io.IOException

object ConfigManager {

    private val file by lazy { getDataFolder().resolve("config.yml") }

    val DEFAULT_CONFIG by lazy { AVMConfig() }

    lateinit var config: AVMConfig

    /**
     * Loads the configuration from the file.
     *
     * @param reload Whether to reload the configuration. Default is false.
     * @return True if the configuration is loaded successfully, false otherwise.
     */
    fun load(reload: Boolean = false): Boolean {
        if (!file.exists() && !save(initialize = true)) return false

        logger.info("{} configuration...", if (reload) "Reloading" else "Loading")

        return try {
            config = YAML.decodeFromString<AVMConfig>(file.readTextWithBuffer())
            // TODO Migrate config
            validate()
            true
        } catch (e: IOException) {
            logger.error("Failed to read configuration file", e)
            false
        } catch (e: YamlException) {
            logger.error(
                "Failed to decode configuration content from file: {} (line {}, column {})",
                e.path.toHumanReadableString(),
                e.line,
                e.column
            )
            logger.error(e.message)
            false
        }
    }

    /**
     * Saves the configuration to the file.
     *
     * @param initialize Whether to generate a default configuration if the file does not exist. Default is false.
     * @return True if the configuration is saved successfully, false otherwise.
     */
    fun save(initialize: Boolean = false): Boolean {
        if (!file.exists()) {
            logger.info(
                "Configuration file does not exist{}",
                if (initialize) ", generating default configuration..." else ""
            )
        }

        return try {
            file.parentFile.mkdirs()
            file.writeTextWithBuffer(YAML.encodeToString(if (!initialize) config else DEFAULT_CONFIG))
            true
        } catch (e: IOException) {
            logger.error("Failed to save configuration to file", e)
            false
        } catch (e: SerializationException) {
            logger.error("Failed to encode configuration", e)
            false
        }
    }

    fun reload(): Boolean = load(reload = true)

    private fun validate() {
        // Validate Chat-Bridge Passthrough mode name
        try {
            ChatBridge.mode = ChatBridge.PassthroughMode.of(config.chatBridge.chatPassthrough.mode)
        } catch (_: IllegalArgumentException) {
            logger.warn("Invalid Chat-Passthrough mode name!")
            logger.warn("Plugin will fallback to `ALL` mode")
            ChatBridge.mode = ChatBridge.PassthroughMode.ALL
        }

        // Validate UUID query API URL
        if (!config.whitelist.queryApi.uuid.isValidUrl()) {
            config.whitelist.queryApi.uuid = DEFAULT_CONFIG.whitelist.queryApi.uuid
            logger.warn("Invalid UUID query API URL!")
            logger.warn("Plugin will fallback to default URL")
        }

        // Validate Profile query API URL
        if (!config.whitelist.queryApi.profile.isValidUrl()) {
            config.whitelist.queryApi.profile = DEFAULT_CONFIG.whitelist.queryApi.profile
            logger.warn("Invalid Profile query API URL!")
            logger.warn("Plugin will fallback to default URL")
        }

        // Validate Cache-Players max size
        if (config.whitelist.cachePlayers.maxSize < 1) {
            config.whitelist.cachePlayers.maxSize = DEFAULT_CONFIG.whitelist.cachePlayers.maxSize
            logger.warn("Invalid Cache-Players max size!")
            logger.warn("Plugin will fallback to default max size")
        }
    }

//    private fun migrate() {
//        val currentVersion = YAML.decodeFromString<Version>(file.readTextWithBuffer()).version
//
//        if (currentVersion == 1 && DEFAULT_CONFIG.version == 1) {
//            config = config.copy(version = 2)
//        }
//    }
}
