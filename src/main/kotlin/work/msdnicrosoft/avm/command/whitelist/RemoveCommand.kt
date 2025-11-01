package work.msdnicrosoft.avm.command.whitelist

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.module.whitelist.result.RemoveResult
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.data.server.Server
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.util.server.task
import work.msdnicrosoft.avm.util.string.isUuid
import work.msdnicrosoft.avm.util.string.toUuid
import java.util.Optional

object RemoveCommand {
    private inline val config get() = ConfigManager.config.whitelist

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
                    val server: Server by this
                    val player: String by this
                    context.source.removePlayer(player, server.name)
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
        val isUuid: Boolean = playerName.isUuid()
        val result: RemoveResult = if (isUuid) {
            WhitelistManager.remove(playerName.toUuid(), serverName)
        } else {
            WhitelistManager.remove(playerName, serverName)
        }

        val message: Component = when (result) {
            RemoveResult.SUCCESS -> {
                if (serverName != null) {
                    tr("avm.command.avmwl.remove.success.server") {
                        args {
                            string("server", serverName)
                            string("player", playerName)
                        }
                    }
                } else {
                    tr("avm.command.avmwl.remove.success.full") {
                        args { string("player", playerName) }
                    }
                }
            }

            RemoveResult.FAIL_NOT_FOUND -> tr("avm.command.avmwl.remove.not_found")
            RemoveResult.SAVE_FILE_FAILED -> tr("avm.whitelist.save.failed")
        }
        this.sendMessage(message)

        if (config.enabled) {
            task {
                val player: Optional<Player> = if (isUuid) {
                    server.getPlayer(playerName.toUuid())
                } else {
                    server.getPlayer(playerName)
                }
                player.ifPresent {
                    if (!WhitelistManager.isListed(it.uniqueId, it.currentServer.get().serverInfo.name)) {
                        kickPlayers(config.message, it)
                    }
                }
            }
        }
    }
}
