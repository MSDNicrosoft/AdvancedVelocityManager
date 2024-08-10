package work.msdnicrosoft.avm.config

import kotlinx.serialization.SerializationException
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.common.util.unsafeLazy
import work.msdnicrosoft.avm.module.chatbridge.ChatBridge
import work.msdnicrosoft.avm.util.FileUtil.decodeFromString
import work.msdnicrosoft.avm.util.FileUtil.encodeToString
import work.msdnicrosoft.avm.util.FileUtil.readTextWithBuffer
import work.msdnicrosoft.avm.util.FileUtil.writeTextWithBuffer
import work.msdnicrosoft.avm.util.FileUtil.yaml
import java.io.IOException

object ConfigManager {

    val file by unsafeLazy { getDataFolder().resolve("config.yml") }

    var config = AVMConfig()

    fun load(reload: Boolean = false): Boolean {
        if (!file.exists()) return save(initialize = true)

        info("${if (reload) "Reloading" else "Loading"} configuration...")
        return try {
            config = yaml.decodeFromString<AVMConfig>(file.readTextWithBuffer())
            // TODO Migrate config
            validate()
            true
        } catch (e: IOException) {
            error("Failed to read configuration file")
            error(e)
        } catch (e: SerializationException) {
            error("Failed to decode configuration content from file")
            error(e)
        }
    }

    fun save(initialize: Boolean = false): Boolean {
        if (!file.exists()) {
            info("Configuration file does not exist${if (initialize) ", generating default configuration..." else ""}")
        }

        return try {
            file.parentFile.mkdirs()
            file.writeTextWithBuffer(yaml.encodeToString(if (!initialize) config else AVMConfig()))
            true
        } catch (e: IOException) {
            error("Failed to save configuration to file")
            error(e)
            false
        } catch (e: SerializationException) {
            error("Failed to encode configuration")
            error(e)
            false
        }
    }

    fun reload() = load(reload = true)

    fun validate() {
        try {
            ChatBridge.mode = ChatBridge.PassthroughMode.valueOf(config.chatBridge.chatPassthrough.mode.uppercase())
        } catch (_: IllegalArgumentException) {
            warning("Incorrect Chat-Passthrough mode name!")
            warning("Plugin will fallback to `ALL` mode")
            ChatBridge.mode = ChatBridge.PassthroughMode.ALL
        }
    }
}
