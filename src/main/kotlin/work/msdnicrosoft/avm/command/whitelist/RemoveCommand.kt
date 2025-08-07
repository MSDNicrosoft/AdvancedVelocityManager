package work.msdnicrosoft.avm.command.whitelist

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.ConfigUtil.isValidServer
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.util.server.task
import work.msdnicrosoft.avm.util.string.isUuid
import work.msdnicrosoft.avm.util.string.toUuid

object RemoveCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command = literalCommand("remove") {
        requires { hasPermission("avm.command.whitelist.remove") }
        wordArgument("player") {
            suggests { builder ->
                WhitelistManager.usernames.forEach(builder::suggest)
                builder.buildFuture()
            }
            executes {
                val player: String by this
                context.source.removePlayer(player)
                Command.SINGLE_SUCCESS
            }
            wordArgument("server") {
                suggests { builder ->
                    val player: String by this
                    val serverList = if (player.isUuid()) {
                        WhitelistManager.getPlayer(player.toUuid())
                    } else {
                        WhitelistManager.getPlayer(player)
                    }?.serverList
                    serverList?.forEach(builder::suggest)
                    builder.buildFuture()
                }
                executes {
                    val server: String by this
                    val player: String by this
                    if (!isValidServer(server)) {
                        context.source.sendTranslatable(
                            "avm.general.not.exist.server",
                            Argument.string("server", server)
                        )
                        return@executes Command.SINGLE_SUCCESS
                    }
                    context.source.removePlayer(player, server)
                    Command.SINGLE_SUCCESS
                }
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
    private fun CommandSource.removePlayer(playerName: String, serverName: String? = null) {
        val isUuid = playerName.isUuid()
        val result = if (isUuid) {
            WhitelistManager.remove(playerName.toUuid(), serverName)
        } else {
            WhitelistManager.remove(playerName, serverName)
        }

        val message = when (result) {
            WhitelistManager.RemoveResult.SUCCESS -> {
                if (serverName != null) {
                    tr(
                        "avm.command.avmwl.remove.success.server",
                        Argument.string("server", serverName),
                        Argument.string("player", playerName)
                    )
                } else {
                    tr(
                        "avm.command.avmwl.remove.success.full",
                        Argument.string("player", playerName)
                    )
                }
            }

            WhitelistManager.RemoveResult.FAIL_NOT_FOUND -> tr("avm.command.avmwl.remove.not.found")
            WhitelistManager.RemoveResult.SAVE_FILE_FAILED -> tr("avm.whitelist.save.failed")
        }
        this.sendMessage(message)

        if (config.enabled) {
            task {
                val player = if (isUuid) {
                    getPlayer(playerName.toUuid())
                } else {
                    getPlayer(playerName)
                }
                player.ifPresent {
                    if (!WhitelistManager.isInServerWhitelist(it.uniqueId, it.currentServer.get().serverInfo.name)) {
                        kickPlayers(config.message, it)
                    }
                }
            }
        }
    }
}
