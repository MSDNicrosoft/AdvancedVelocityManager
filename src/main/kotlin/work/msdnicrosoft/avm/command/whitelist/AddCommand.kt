package work.msdnicrosoft.avm.command.whitelist

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.bool
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager.AddResult
import work.msdnicrosoft.avm.util.ConfigUtil.isValidServer
import work.msdnicrosoft.avm.util.string.isUuid
import work.msdnicrosoft.avm.util.string.toUuid
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@PlatformSide(Platform.VELOCITY)
object AddCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ ->
                (PlayerCache.readOnly + AVM.plugin.server.allPlayers.map { it.username } + WhitelistManager.usernames)
                    .distinct()
            }
            dynamic("server") {
                suggestion<ProxyCommandSender>(uncheck = true) { _, context ->
                    val player = context["player"]
                    val whitelistedServers = if (player.isUuid()) {
                        WhitelistManager.getPlayer(player.toUuid())
                    } else {
                        WhitelistManager.getPlayer(player)
                    }?.serverList
                    (config.serverGroups.keys + AVM.plugin.server.allServers.map { it.serverInfo.name })
                        .filterNot { whitelistedServers?.contains(it) == true }
                        .distinct()
                }
                bool("onlineMode") {
                    execute<ProxyCommandSender> { sender, context, _ ->
                        sender.addPlayer(context["player"], context["server"], context.bool("onlineMode"))
                    }
                }
                execute<ProxyCommandSender> { sender, context, _ ->
                    sender.addPlayer(context["player"], context["server"])
                }
            }
        }
    }

    private fun ProxyCommandSender.addPlayer(player: String, serverName: String, onlineMode: Boolean? = null) {
        if (!isValidServer(serverName)) {
            sendLang("server-not-found", serverName)
            return
        }

        val isUuid = player.isUuid()
        if (isUuid && !WhitelistManager.serverIsOnlineMode) {
            sendLang("command-avmwl-add-uuid-unsupported")
            return
        }

        val result = if (isUuid) {
            WhitelistManager.add(player.toUuid(), serverName, onlineMode)
        } else {
            WhitelistManager.add(player, serverName, onlineMode)
        }

        when (result) {
            AddResult.SUCCESS -> sendLang("command-avmwl-add-success", serverName, player)
            AddResult.API_LOOKUP_NOT_FOUND -> sendLang("command-avmwl-add-request-not-found")
            AddResult.API_LOOKUP_REQUEST_FAILED -> sendLang("command-avmwl-add-request-failed")
            AddResult.ALREADY_EXISTS -> sendLang("command-avmwl-add-already-exists")
            AddResult.SAVE_FILE_FAILED -> sendLang("command-avmwl-save-failed")
        }
    }
}
