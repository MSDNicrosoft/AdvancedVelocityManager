package work.msdnicrosoft.avm.config

import com.charleskorn.kaml.YamlException
import kotlinx.serialization.SerializationException
import taboolib.common.platform.function.getDataFolder
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.logger
import work.msdnicrosoft.avm.module.chatbridge.ChatBridge
import work.msdnicrosoft.avm.util.FileUtil.decodeFromString
import work.msdnicrosoft.avm.util.FileUtil.encodeToString
import work.msdnicrosoft.avm.util.FileUtil.readTextWithBuffer
import work.msdnicrosoft.avm.util.FileUtil.writeTextWithBuffer
import work.msdnicrosoft.avm.util.FileUtil.yaml
import java.io.IOException

object ConfigManager {

    private val file by lazy { getDataFolder().resolve("config.yml") }

    val DEFAULT_CONFIG by lazy { AVMConfig() }

    var config = AVMConfig()

    /**
     * Loads the configuration from the file.
     *
     * @param reload Whether to reload the configuration. Default is false.
     * @return True if the configuration is loaded successfully, false otherwise.
     */
    fun load(reload: Boolean = false): Boolean {
        if (!file.exists() && !save(initialize = true)) return false

        logger.info("${if (reload) "Reloading" else "Loading"} configuration...")
        return try {
            config = yaml.decodeFromString<AVMConfig>(file.readTextWithBuffer())
            // TODO Migrate config
            validate()
            true
        } catch (e: IOException) {
            logger.error("Failed to read configuration file", e)
            false
        } catch (e: YamlException) {
            logger.error("Failed to decode configuration content from file")
            logger.error("${e.path.toHumanReadableString()} (line ${e.line}, column ${e.column}) of file is incorrect")
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
                "Configuration file does not exist${if (initialize) ", generating default configuration..." else ""}"
            )
        }

        return try {
            file.parentFile.mkdirs()
            file.writeTextWithBuffer(yaml.encodeToString(if (!initialize) config else DEFAULT_CONFIG))
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

    fun validate() {
        try {
            ChatBridge.mode = ChatBridge.PassthroughMode.valueOf(config.chatBridge.chatPassthrough.mode.uppercase())
        } catch (_: IllegalArgumentException) {
            logger.warn("Incorrect Chat-Passthrough mode name!")
            logger.warn("Plugin will fallback to `ALL` mode")
            ChatBridge.mode = ChatBridge.PassthroughMode.ALL
        }
    }
}
