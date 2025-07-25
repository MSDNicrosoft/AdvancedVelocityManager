package work.msdnicrosoft.avm.module.imports.importers

import com.moandjiezana.toml.Toml
import kotlinx.serialization.Serializable
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendError
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.data.UUIDSerializer
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import java.util.UUID
import kotlin.io.path.exists

@PlatformSide(Platform.VELOCITY)
object QuAnVelocityWhitelistImporter : Importer {

    override val pluginName = "(qu-an) VelocityWhitelist"

    @Serializable
    private data class Player(
        @Serializable(with = UUIDSerializer::class)
        val uuid: UUID,
        val name: String
    )

    private val PATH by lazy { plugin.configDirectory.parent.resolve("VelocityWhitelist") }
    private val CONFIG_PATH by lazy { PATH.resolve("config.toml") }
    private val WHITELIST_PATH by lazy {
        if (WhitelistManager.serverIsOnlineMode) {
            PATH.resolve("whitelist.json")
        } else {
            PATH.resolve("whitelist_offline.json")
        }
    }

    override fun import(sender: ProxyCommandSender, defaultServer: String): Boolean {
        val configSuccess = if (CONFIG_PATH.exists()) {
            sender.importConfig()
        } else {
            sender.sendLang("command-avm-import-config-does-not-exist", pluginName)
            false
        }

        val whitelistSuccess = if (WHITELIST_PATH.exists()) {
            sender.importWhitelist(defaultServer)
        } else {
            sender.sendLang("command-avm-import-whitelist-does-not-exist", pluginName)
            false
        }

        return configSuccess && whitelistSuccess
    }

    private fun ProxyCommandSender.importConfig(): Boolean =
        try {
            val config = Toml().read(CONFIG_PATH.readTextWithBuffer())
            ConfigManager.config.run {
                whitelist.enabled = config.getBoolean("enable_whitelist")
                whitelist.queryApi.uuid = config.getString("uuid_api")
                whitelist.queryApi.profile = config.getString("profile_api")
            }
            ConfigManager.save()
        } catch (e: Exception) {
            sendError("command-avm-import-config-failed", pluginName, e.message ?: asLangText("unknown-cause"))
            false
        }

    private fun ProxyCommandSender.importWhitelist(defaultServer: String): Boolean =
        try {
            val whitelist = JSON.decodeFromString<List<Player>>(WHITELIST_PATH.readTextWithBuffer())

            val onlineMode = WhitelistManager.serverIsOnlineMode

            whitelist.forEach { player ->
                WhitelistManager.add(player.name, player.uuid, defaultServer, onlineMode)
            }
            true
        } catch (e: Exception) {
            sendError("command-avm-import-whitelist-failed", pluginName, e.message ?: asLangText("unknown-cause"))
            false
        }
}
