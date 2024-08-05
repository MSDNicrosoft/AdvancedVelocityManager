package work.msdnicrosoft.avm.util.importer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.ConfigUtil
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

object LlsManagerUtil {

    @Serializable
    data class Config(
        val showAllPlayerInTabList: Boolean,
        val bridgeChatMessage: Boolean,
        val bridgePlayerJoinMessage: Boolean,
        val bridgePlayerLeaveMessage: Boolean,

        @SerialName("whitelist")
        val whitelistEnabled: Boolean,

        val serverGroup: Map<String, List<String>>
    )

    @Serializable
    data class PlayerData(
        @SerialName("whitelistServerList")
        val serverList: List<String>,
        val onlineMode: Boolean
    )

    val PATH = AVM.plugin.configDirectory.parent.resolve("lls-manager")
    val CONFIG_PATH = PATH.resolve("config.json")
    val PLAYER_DATA_PATH = PATH.resolve("player")

    fun import(defaultServer: String): Boolean {
        var success = true
        try {
            val config = ConfigUtil.json.decodeFromString<Config>(CONFIG_PATH.readText())

            // Override AVM config
            AVM.config.run {
                tabSync.enabled = config.showAllPlayerInTabList
                chatBridge.enabled = config.bridgeChatMessage
                broadcast.join.enabled = config.bridgePlayerJoinMessage
                broadcast.leave.enabled = config.bridgePlayerLeaveMessage
                whitelist.enabled = config.whitelistEnabled
                whitelist.serverGroups = config.serverGroup
            }
            AVM.saveConfig()
        } catch (e: Exception) {
            error("Failed to import config from lls-manager: ${e.message}")
            success = false
        }

        PLAYER_DATA_PATH.listDirectoryEntries().filter { it.extension.lowercase() == "json" }.forEach { file ->
            file.let {
                val username = file.nameWithoutExtension
                val llsPlayer = try {
                    ConfigUtil.json.decodeFromString<PlayerData>(file.readText())
                } catch (e: Exception) {
                    error("Failed to import player $username from lls-manager: ${e.message}")
                    success = false
                    return@let
                }

                if (llsPlayer.serverList.isEmpty()) {
                    WhitelistManager.add(username, defaultServer, llsPlayer.onlineMode)
                    return@let
                }

                llsPlayer.serverList.forEach { server ->
                    WhitelistManager.add(username, server, llsPlayer.onlineMode)
                }
            }
        }
        return success
    }
}
