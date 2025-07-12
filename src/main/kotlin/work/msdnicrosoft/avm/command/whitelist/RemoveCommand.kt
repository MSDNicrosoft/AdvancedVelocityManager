package work.msdnicrosoft.avm.command.whitelist

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submitAsync
import taboolib.common.util.presentRun
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.ConfigUtil.isValidServer
import work.msdnicrosoft.avm.util.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.util.string.isUuid
import work.msdnicrosoft.avm.util.string.toUuid

@PlatformSide(Platform.VELOCITY)
object RemoveCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                WhitelistManager.usernames.toList()
            }
            dynamic("server") {
                suggestion<ProxyCommandSender>(uncheck = true) { _, context ->
                    val player = context["player"]
                    if (player.isUuid()) {
                        WhitelistManager.getPlayer(player.toUuid())
                    } else {
                        WhitelistManager.getPlayer(player)
                    }?.serverList
                }
                execute<ProxyCommandSender> { sender, context, _ ->
                    val serverName = context["server"]
                    if (!isValidServer(serverName)) {
                        sender.sendLang("server-not-found", serverName)
                        return@execute
                    }
                    sender.removePlayer(context["player"], serverName)
                }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                sender.removePlayer(context["player"])
            }
        }
    }

    /**
     * Removes a player from the whitelist.
     *
     * @param playerName The name or UUID of the player to remove.
     * @param serverName The name of the server to remove the player from.
     * If null, the player will be removed from all servers.
     */
    private fun ProxyCommandSender.removePlayer(playerName: String, serverName: String? = null) {
        val isUuid = playerName.isUuid()
        val result = if (isUuid) {
            WhitelistManager.remove(playerName.toUuid(), serverName)
        } else {
            WhitelistManager.remove(playerName, serverName)
        }

        when (result) {
            WhitelistManager.RemoveResult.SUCCESS -> {
                if (serverName != null) {
                    sendLang("command-avmwl-remove-server-success", serverName, playerName)
                } else {
                    sendLang("command-avmwl-remove-full-success", playerName)
                }
            }

            WhitelistManager.RemoveResult.FAIL_NOT_FOUND -> sendLang("command-avmwl-remove-not-found")
            WhitelistManager.RemoveResult.SAVE_FILE_FAILED -> sendLang("command-avmwl-save-failed")
        }

        if (config.enabled) {
            submitAsync(now = true) {
                val player = if (isUuid) {
                    getPlayer(playerName.toUuid())
                } else {
                    getPlayer(playerName)
                }
                player.presentRun {
                    if (!WhitelistManager.isInServerWhitelist(uniqueId, currentServer.get().serverInfo.name)) {
                        kickPlayers(config.message, this)
                    }
                }
            }
        }
    }
}
