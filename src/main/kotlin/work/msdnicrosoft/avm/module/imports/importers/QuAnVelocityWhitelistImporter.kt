package work.msdnicrosoft.avm.module.imports.importers

import com.moandjiezana.toml.Toml
import com.velocitypowered.api.command.CommandSource
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.component.sendTranslatable
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

    override fun import(source: CommandSource, defaultServer: String): Boolean {
        val configSuccess: Boolean = if (this.CONFIG_PATH.exists()) {
            importConfig(source)
        } else {
            source.sendTranslatable(
                "avm.command.avm.import.config.not.exist",
                Argument.string("plugin_name", this.pluginName)
            )
            true
        }

        val whitelistSuccess: Boolean = if (this.WHITELIST_PATH.exists()) {
            importWhitelist(defaultServer, source)
        } else {
            source.sendTranslatable(
                "avm.command.avm.import.whitelist.not.exist",
                Argument.string("plugin_name", this.pluginName)
            )
            true
        }

        return configSuccess && whitelistSuccess
    }

    private fun importConfig(source: CommandSource): Boolean =
        try {
            val config: Toml = TOML.read(this.CONFIG_PATH.readTextWithBuffer())
            ConfigManager.config.run {
                whitelist.enabled = config.getBoolean("enable_whitelist")
                whitelist.queryApi.uuid = config.getString("uuid_api")
                whitelist.queryApi.profile = config.getString("profile_api")
            }
            ConfigManager.save()
        } catch (e: Exception) {
            source.sendTranslatable(
                "avm.command.avm.import.config.failed",
                Argument.string("plugin_name", this.pluginName),
                Argument.string("reason", e.message.orEmpty())
            )
            false
        }

    private fun importWhitelist(defaultServer: String, source: CommandSource): Boolean =
        try {
            val whitelist: List<Player> = JSON.decodeFromString(this.WHITELIST_PATH.readTextWithBuffer())
            val onlineMode: Boolean = WhitelistManager.serverIsOnlineMode

            whitelist.forEach { player ->
                WhitelistManager.add(player.name, player.uuid, defaultServer, onlineMode)
            }
            true
        } catch (e: Exception) {
            source.sendTranslatable(
                "avm.command.avm.import.whitelist.failed",
                Argument.string("plugin_name", this.pluginName),
                Argument.string("reason", e.message.orEmpty())
            )
            false
        }
}
