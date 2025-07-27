package work.msdnicrosoft.avm.module.imports.importers

import com.moandjiezana.toml.Toml
import com.velocitypowered.api.command.CommandSource
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.sendTranslatable
import work.msdnicrosoft.avm.util.data.UUIDSerializer
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import java.util.*
import kotlin.io.path.div
import kotlin.io.path.exists

object QuAnVelocityWhitelistImporter : Importer {

    override val pluginName = "(qu-an) VelocityWhitelist"

    @Serializable
    private data class Player(
        @Serializable(with = UUIDSerializer::class)
        val uuid: UUID,
        val name: String
    )

    private val PATH = dataDirectory.parent / "VelocityWhitelist"
    private val CONFIG_PATH = PATH / "config.toml"
    private val WHITELIST_PATH =
        if (WhitelistManager.serverIsOnlineMode) {
            PATH / "whitelist.json"
        } else {
            PATH / "whitelist_offline.json"
        }

    override fun import(source: CommandSource, defaultServer: String): Boolean {
        val configSuccess = if (CONFIG_PATH.exists()) {
            source.importConfig()
        } else {
            source.sendTranslatable(
                "avm.command.avm.import.config.not.exist",
                Argument.string("plugin_name", pluginName)
            )
            false
        }

        val whitelistSuccess = if (WHITELIST_PATH.exists()) {
            source.importWhitelist(defaultServer)
        } else {
            source.sendTranslatable(
                "avm.command.avm.import.whitelist.not.exist",
                Argument.string("plugin_name", pluginName)
            )
            false
        }

        return configSuccess && whitelistSuccess
    }

    private fun CommandSource.importConfig(): Boolean =
        try {
            val config = Toml().read(CONFIG_PATH.readTextWithBuffer())
            ConfigManager.config.run {
                whitelist.enabled = config.getBoolean("enable_whitelist")
                whitelist.queryApi.uuid = config.getString("uuid_api")
                whitelist.queryApi.profile = config.getString("profile_api")
            }
            ConfigManager.save()
        } catch (e: Exception) {
            sendTranslatable(
                "avm.command.avm.import.config.failed",
                Argument.string("plugin_name", pluginName),
                Argument.string("reason", e.message.orEmpty())
            )
            false
        }

    private fun CommandSource.importWhitelist(defaultServer: String): Boolean =
        try {
            val whitelist = JSON.decodeFromString<List<Player>>(WHITELIST_PATH.readTextWithBuffer())

            val onlineMode = WhitelistManager.serverIsOnlineMode

            whitelist.forEach { player ->
                WhitelistManager.add(player.name, player.uuid, defaultServer, onlineMode)
            }
            true
        } catch (e: Exception) {
            sendTranslatable(
                "avm.command.avm.import.whitelist.failed",
                Argument.string("plugin_name", pluginName),
                Argument.string("reason", e.message.orEmpty())
            )
            false
        }
}
