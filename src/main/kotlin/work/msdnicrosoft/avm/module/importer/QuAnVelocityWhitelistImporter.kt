package work.msdnicrosoft.avm.module.importer

import com.moandjiezana.toml.Toml
import kotlinx.serialization.Serializable
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendError
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.interfaces.Importer
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.FileUtil.json
import work.msdnicrosoft.avm.util.FileUtil.readTextWithBuffer
import work.msdnicrosoft.avm.util.data.UUIDSerializer
import java.util.UUID
import kotlin.io.path.exists
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@PlatformSide(Platform.VELOCITY)
object QuAnVelocityWhitelistImporter : Importer {

    override val pluginName = "(qu-an) VelocityWhitelist"

    @Serializable
    data class Player(
        @Serializable(with = UUIDSerializer::class)
        val uuid: UUID,
        val name: String
    )

    val PATH = AVM.plugin.configDirectory.parent.resolve("VelocityWhitelist")
    val CONFIG_PATH = PATH.resolve("config.toml")
    val WHITELIST_FILE_PATH =
        PATH.resolve(if (WhitelistManager.serverIsOnlineMode) "whitelist.json" else "whitelist_offline.json")

    override fun ProxyCommandSender.import(defaultServer: String): Boolean {
        var success = true
        try {
            if (!CONFIG_PATH.exists()) {
                sendLang("command-avm-import-config-does-not-exist", pluginName)
            } else {
                val config = Toml().read(CONFIG_PATH.readTextWithBuffer())

                ConfigManager.config.run {
                    whitelist.enabled = config.getBoolean("enable_whitelist")
                    whitelist.queryApi.uuid = config.getString("uuid_api")
                    whitelist.queryApi.profile = config.getString("profile_api")
                }
                ConfigManager.save()
            }
        } catch (e: Exception) {
            success = false
            sendError("command-avm-import-config-failed", pluginName, e.message ?: asLangText("unknown-cause"))
        }

        if (!WHITELIST_FILE_PATH.exists()) {
            sendLang("command-avm-import-whitelist-does-not-exist", pluginName)
        } else {
            val whitelist = try {
                json.decodeFromString<List<Player>>(WHITELIST_FILE_PATH.readTextWithBuffer())
            } catch (e: Exception) {
                sendError("command-avm-import-whitelist-failed", pluginName, e.message ?: asLangText("unknown-cause"))
                return false
            }

            val onlineMode = WhitelistManager.serverIsOnlineMode

            whitelist.forEach { player ->
                WhitelistManager.add(player.name, player.uuid, defaultServer, onlineMode)
            }
        }
        return success
    }
}
