package work.msdnicrosoft.avm.module.importer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendError
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.interfaces.Importer
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.FileUtil.json
import work.msdnicrosoft.avm.util.FileUtil.readTextWithBuffer
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

object LlsManagerImporter : Importer {

    override val pluginName = "lls-manager"

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

    @Suppress("LoopWithTooManyJumpStatements")
    override fun ProxyCommandSender.import(defaultServer: String): Boolean {
        var success = true
        try {
            if (!CONFIG_PATH.exists()) {
                sendLang("command-avm-import-config-does-not-exist", pluginName)
            } else {
                val config = json.decodeFromString<Config>(CONFIG_PATH.readTextWithBuffer())

                ConfigManager.config.run {
                    tabSync.enabled = config.showAllPlayerInTabList
                    chatBridge.enabled = config.bridgeChatMessage
                    broadcast.join.enabled = config.bridgePlayerJoinMessage
                    broadcast.leave.enabled = config.bridgePlayerLeaveMessage
                    whitelist.enabled = config.whitelistEnabled
                    whitelist.serverGroups = config.serverGroup
                }
                ConfigManager.save()
            }
        } catch (e: Exception) {
            sendError("command-avm-import-config-failed", pluginName, e.message ?: asLangText("unknown-cause"))
            success = false
        }

        if (!PLAYER_DATA_PATH.exists()) {
            sendLang("command-avm-import-player-does-not-exist", pluginName)
        } else {
            val files = PLAYER_DATA_PATH.listDirectoryEntries().filter { it.extension.lowercase() == "json" }
            for (file in files) {
                val username = file.nameWithoutExtension
                val llsPlayer = try {
                    json.decodeFromString<PlayerData>(file.readTextWithBuffer())
                } catch (e: Exception) {
                    sendError(
                        "command-avm-import-player-data-failed",
                        username,
                        pluginName,
                        e.message ?: asLangText("unknown-cause")
                    )
                    success = false
                    continue
                }

                if (llsPlayer.serverList.isEmpty()) {
                    WhitelistManager.add(username, defaultServer, llsPlayer.onlineMode)
                    continue
                }

                llsPlayer.serverList.forEach { server ->
                    WhitelistManager.add(username, server, llsPlayer.onlineMode)
                }
            }
        }
        return success
    }
}
