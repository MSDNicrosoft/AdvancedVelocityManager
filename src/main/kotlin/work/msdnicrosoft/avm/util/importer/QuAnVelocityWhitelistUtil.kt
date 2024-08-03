package work.msdnicrosoft.avm.util.importer

import com.moandjiezana.toml.Toml
import kotlinx.serialization.Serializable
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.ConfigUtil
import work.msdnicrosoft.avm.util.data.UUIDSerializer
import java.util.UUID
import kotlin.io.path.readText
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

object QuAnVelocityWhitelistUtil {

    @Serializable
    data class Player(
        @Serializable(with = UUIDSerializer::class)
        val uuid: UUID,
        val name: String
    )

    val PATH = AVM.plugin.configDirectory.parent.resolve("VelocityWhitelist")
    val CONFIG_PATH = PATH.resolve("config.toml")
    val WHITELIST_FILE_PATH = PATH.resolve(
        if (WhitelistManager.serverIsOnlineMode) "whitelist.json" else "whitelist_offline.json"
    )

    fun import(defaultServer: String): Boolean {
        var success = true
        try {
            val config = Toml().read(CONFIG_PATH.readText())

            AVM.config.run {
                whitelist.enabled = config.getBoolean("enable_whitelist")
                whitelist.queryApi.uuid = config.getString("uuid_api")
                whitelist.queryApi.profile = config.getString("profile_api")
            }
            AVM.saveConfig()
        } catch (e: Exception) {
            success = false
            error("An error occurred while importing config from lls-manager: ${e.message}")
        }

        val whitelist = try {
            ConfigUtil.json.decodeFromString<List<Player>>(WHITELIST_FILE_PATH.readText())
        } catch (e: Exception) {
            error("An error occurred while importing players from lls-manager: ${e.message}")
            return false
        }

        val onlineMode = WhitelistManager.serverIsOnlineMode

        whitelist.forEach { player ->
            WhitelistManager.add(player.name, player.uuid, defaultServer, onlineMode)
        }
        return success
    }
}
