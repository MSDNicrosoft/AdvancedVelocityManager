package work.msdnicrosoft.avm.module.imports.importers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.context.CommandContext
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import java.nio.file.Path
import kotlin.io.path.*

object LlsManagerImporter : Importer {
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

    private val PATH: Path = dataDirectory.parent / "lls-manager"
    private val CONFIG_PATH: Path = PATH / "config.json"
    private val PLAYER_DATA_PATH: Path = PATH / "player"

    override val displayName: String = "lls-manager"

    override fun import(context: CommandContext, defaultServer: String): Boolean {
        val configSuccess: Boolean = if (this.CONFIG_PATH.exists()) {
            context.importConfig()
        } else {
            context.sendTranslatable("avm.command.avm.import.config.not_exist") {
                args { string("plugin_name", displayName) }
            }
            true
        }

        val playerDataSuccess: Boolean = if (PLAYER_DATA_PATH.exists()) {
            context.importPlayerData(defaultServer)
        } else {
            context.sendTranslatable("avm.command.avm.import.player.not_exist") {
                args { string("plugin_name", displayName) }
            }
            true
        }

        return configSuccess && playerDataSuccess
    }

    private fun CommandContext.importConfig(): Boolean =
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
            sendTranslatable("avm.command.avm.import.config.failed") {
                args {
                    string("plugin_name", displayName)
                    string("reason", e.message.orEmpty())
                }
            }
            false
        }

    private fun CommandContext.importPlayerData(defaultServer: String): Boolean {
        var success = true

        PLAYER_DATA_PATH.listDirectoryEntries().asSequence()
            .filter { file -> file.extension.equals("json", ignoreCase = true) && file.isRegularFile() }
            .forEach { file ->
                val username: String = file.nameWithoutExtension
                try {
                    val llsPlayer = JSON.decodeFromString<PlayerData>(file.readTextWithBuffer())
                    val servers: List<String> = llsPlayer.serverList.ifEmpty { listOf(defaultServer) }
                    servers.forEach { server ->
                        WhitelistManager.add(username, server, llsPlayer.onlineMode)
                    }
                } catch (e: Exception) {
                    sendTranslatable("avm.command.avm.import.player.failed") {
                        args {
                            string("player", username)
                            string("plugin_name", displayName)
                            string("reason", e.message.orEmpty())
                        }
                    }
                    success = false
                }
            }
        return success
    }
}
