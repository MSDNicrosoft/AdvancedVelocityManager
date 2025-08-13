package work.msdnicrosoft.avm.module.imports.importers

import com.velocitypowered.api.command.CommandSource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import kotlin.io.path.*

object LlsManagerImporter : Importer("lls-manager") {
    @Serializable
    private data class Config(
        val showAllPlayerInTabList: Boolean,
        val bridgeChatMessage: Boolean,
        val bridgePlayerJoinMessage: Boolean,
        val bridgePlayerLeaveMessage: Boolean,

        @SerialName("whitelist")
        val whitelistEnabled: Boolean,

        val serverGroup: Map<String, List<String>>
    )

    @Serializable
    private data class PlayerData(
        @SerialName("whitelistServerList")
        val serverList: List<String>,
        val onlineMode: Boolean
    )

    private val PATH = dataDirectory.parent / "lls-manager"
    private val CONFIG_PATH = PATH / "config.json"
    private val PLAYER_DATA_PATH = PATH / "player"

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

        val playerDataSuccess = if (PLAYER_DATA_PATH.exists()) {
            source.importPlayerData(defaultServer)
        } else {
            source.sendTranslatable(
                "avm.command.avm.import.player.not.exist",
                Argument.string("plugin_name", pluginName)
            )
            false
        }

        return configSuccess && playerDataSuccess
    }

    private fun CommandSource.importConfig(): Boolean =
        try {
            val config = JSON.decodeFromString<Config>(CONFIG_PATH.readTextWithBuffer())

            ConfigManager.config.apply {
                tabSync.enabled = config.showAllPlayerInTabList
                chatBridge.enabled = config.bridgeChatMessage
                broadcast.join.enabled = config.bridgePlayerJoinMessage
                broadcast.leave.enabled = config.bridgePlayerLeaveMessage
                whitelist.enabled = config.whitelistEnabled
                whitelist.serverGroups = config.serverGroup
            }
            ConfigManager.save()
        } catch (e: Exception) {
            sendTranslatable(
                "avm.command.avm.import.config.failed",
                Argument.string("plugin_name", QuAnVelocityWhitelistImporter.pluginName),
                Argument.string("reason", e.message.orEmpty())
            )
            false
        }

    private fun CommandSource.importPlayerData(defaultServer: String): Boolean {
        var success = true

        PLAYER_DATA_PATH.listDirectoryEntries()
            .asSequence()
            .filter { it.extension.equals("json", ignoreCase = true) && it.isRegularFile() }
            .forEach { file ->
                val username = file.nameWithoutExtension
                try {
                    val llsPlayer = JSON.decodeFromString<PlayerData>(file.readTextWithBuffer())
                    val servers = llsPlayer.serverList.ifEmpty { listOf(defaultServer) }
                    servers.forEach { server ->
                        WhitelistManager.add(username, server, llsPlayer.onlineMode)
                    }
                } catch (e: Exception) {
                    sendTranslatable(
                        "avm.command.avm.import.player.failed",
                        Argument.string("player", username),
                        Argument.string("plugin_name", QuAnVelocityWhitelistImporter.pluginName),
                        Argument.string("reason", e.message.orEmpty())
                    )
                    success = false
                }
            }
        return success
    }
}
