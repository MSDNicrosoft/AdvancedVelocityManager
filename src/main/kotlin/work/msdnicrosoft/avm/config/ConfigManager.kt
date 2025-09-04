package work.msdnicrosoft.avm.config

import com.charleskorn.kaml.YamlException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger
import work.msdnicrosoft.avm.config.data.Version
import work.msdnicrosoft.avm.module.chatbridge.ChatBridge
import work.msdnicrosoft.avm.module.chatbridge.PassthroughMode
import work.msdnicrosoft.avm.util.file.FileUtil.YAML
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import work.msdnicrosoft.avm.util.file.writeTextWithBuffer
import work.msdnicrosoft.avm.util.string.isValidUrl
import java.io.File
import java.io.IOException
import kotlin.io.path.div

object ConfigManager {
    lateinit var config: AVMConfig

    private val FILE: File = (dataDirectory / "config.yml").toFile()
    private val DEFAULT_CONFIG: AVMConfig by lazy { AVMConfig() }

    /**
     * Loads the configuration from the file.
     *
     * @param reload Whether to reload the configuration. Default is false.
     * @return True if the configuration is loaded successfully, false otherwise.
     */
    fun load(reload: Boolean = false): Boolean {
        if (!this.FILE.exists() && !this.save(initialize = true)) return false

        logger.info("{} configuration...", if (reload) "Reloading" else "Loading")

        return try {
            this.migrate()
            this.config = YAML.decodeFromString<AVMConfig>(this.FILE.readTextWithBuffer())
            this.validate()
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
        if (!this.FILE.exists()) {
            logger.info(
                "Configuration file does not exist{}",
                if (initialize) ", generating default configuration..." else ""
            )
        }

        return try {
            this.FILE.parentFile.mkdirs()
            this.FILE.writeTextWithBuffer(YAML.encodeToString(if (!initialize) this.config else this.DEFAULT_CONFIG))
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
            ChatBridge.mode = PassthroughMode.of(config.chatBridge.chatPassthrough.mode)
        } catch (_: IllegalArgumentException) {
            logger.warn("Invalid Chat-Passthrough mode name!")
            logger.warn("Plugin will fallback to `ALL` mode")
            ChatBridge.mode = PassthroughMode.ALL
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

    private fun migrate() {
        val currentVersion = YAML.decodeFromString<Version>(this.FILE.readTextWithBuffer()).version
        if (currentVersion == this.DEFAULT_CONFIG.version) return
        when (currentVersion) {
            1 if this.DEFAULT_CONFIG.version == 2 -> {
                logger.warn("Detected old config version, please migrate config:")
                logger.warn("1. Move your config file to another place and execute `/avm reload` to regenerate config")
                logger.warn("2. Manually compare and migrate configurations")
                logger.warn("3. Execute `/avm reload` again to apply changes ")
                error("Old configuration version")
            }
        }
    }
}
