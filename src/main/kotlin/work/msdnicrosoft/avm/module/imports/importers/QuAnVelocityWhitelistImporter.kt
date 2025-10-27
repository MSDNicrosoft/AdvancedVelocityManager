package work.msdnicrosoft.avm.module.imports.importers

import com.moandjiezana.toml.Toml
import kotlinx.serialization.Serializable
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.context.CommandContext
import work.msdnicrosoft.avm.util.data.UUIDSerializer
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import work.msdnicrosoft.avm.util.file.FileUtil.TOML
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import java.nio.file.Path
import java.util.*
import kotlin.io.path.div
import kotlin.io.path.exists

object QuAnVelocityWhitelistImporter : Importer {
    @Serializable
    private data class Player(
        @Serializable(with = UUIDSerializer::class)
        val uuid: UUID,
        val name: String
    )

    private val PATH: Path = dataDirectory.parent / "VelocityWhitelist"
    private val CONFIG_PATH: Path = this.PATH / "config.toml"
    private val WHITELIST_PATH: Path =
        if (WhitelistManager.serverIsOnlineMode) {
            this.PATH / "whitelist.json"
        } else {
            this.PATH / "whitelist_offline.json"
        }

    override val pluginName: String = "(qu-an) VelocityWhitelist"

    override fun import(context: CommandContext, defaultServer: String): Boolean {
        val configSuccess: Boolean = if (this.CONFIG_PATH.exists()) {
            importConfig(context)
        } else {
            context.sendTranslatable("avm.command.avm.import.config.not_exist") {
                args { string("plugin_name", pluginName) }
            }
            true
        }

        val whitelistSuccess: Boolean = if (this.WHITELIST_PATH.exists()) {
            importWhitelist(defaultServer, context)
        } else {
            context.sendTranslatable("avm.command.avm.import.whitelist.not_exist") {
                args { string("plugin_name", pluginName) }
            }
            true
        }

        return configSuccess && whitelistSuccess
    }

    private fun importConfig(context: CommandContext): Boolean =
        try {
            val config: Toml = TOML.read(this.CONFIG_PATH.readTextWithBuffer())
            ConfigManager.config.run {
                whitelist.enabled = config.getBoolean("enable_whitelist")
                whitelist.queryApi.uuid = config.getString("uuid_api")
                whitelist.queryApi.profile = config.getString("profile_api")
            }
            ConfigManager.save()
        } catch (e: Exception) {
            context.sendTranslatable("avm.command.avm.import.config.failed") {
                args {
                    string("plugin_name", pluginName)
                    string("reason", e.message.orEmpty())
                }
            }
            false
        }

    private fun importWhitelist(defaultServer: String, context: CommandContext): Boolean =
        try {
            val whitelist: List<Player> = JSON.decodeFromString(this.WHITELIST_PATH.readTextWithBuffer())
            val onlineMode: Boolean = WhitelistManager.serverIsOnlineMode

            whitelist.forEach { player ->
                WhitelistManager.add(player.name, player.uuid, defaultServer, onlineMode)
            }
            true
        } catch (e: Exception) {
            context.sendTranslatable("avm.command.avm.import.whitelist.failed") {
                args {
                    string("plugin_name", pluginName)
                    string("reason", e.message.orEmpty())
                }
            }
            false
        }
}
