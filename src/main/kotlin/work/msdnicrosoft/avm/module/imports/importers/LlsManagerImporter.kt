package work.msdnicrosoft.avm.module.imports.importers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendError
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

object LlsManagerImporter : Importer {

    override val pluginName = "lls-manager"

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

    private val PATH by lazy { plugin.configDirectory.parent.resolve("lls-manager") }
    private val CONFIG_PATH by lazy { PATH.resolve("config.json") }
    private val PLAYER_DATA_PATH by lazy { PATH.resolve("player") }

    override fun import(sender: ProxyCommandSender, defaultServer: String): Boolean {
        val configSuccess = if (CONFIG_PATH.exists()) {
            sender.importConfig()
        } else {
            sender.sendLang("command-avm-import-config-does-not-exist", pluginName)
            false
        }

        val playerDataSuccess = if (PLAYER_DATA_PATH.exists()) {
            sender.importPlayerData(defaultServer)
        } else {
            sender.sendLang("command-avm-import-player-does-not-exist", pluginName)
            false
        }

        return configSuccess && playerDataSuccess
    }

    private fun ProxyCommandSender.importConfig(): Boolean =
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
            sendError("command-avm-import-config-failed", pluginName, e.message ?: asLangText("unknown-cause"))
            false
        }

    @Suppress("NestedBlockDepth")
    private fun ProxyCommandSender.importPlayerData(defaultServer: String): Boolean {
        var success = true

        PLAYER_DATA_PATH.listDirectoryEntries()
            .asSequence()
            .filter { it.extension.equals("json", ignoreCase = true) }
            .forEach { file ->
                val username = file.nameWithoutExtension
                try {
                    val llsPlayer = JSON.decodeFromString<PlayerData>(file.readTextWithBuffer())
                    val servers = llsPlayer.serverList.ifEmpty { listOf(defaultServer) }
                    servers.forEach { server ->
                        WhitelistManager.add(username, server, llsPlayer.onlineMode)
                    }
                } catch (e: Exception) {
                    sendError(
                        "command-avm-import-player-data-failed",
                        username,
                        pluginName,
                        e.message ?: asLangText("unknown-cause")
                    )
                    success = false
                }
            }
        return success
    }
}
