package work.msdnicrosoft.avm.command.whitelist

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager.AddResult
import work.msdnicrosoft.avm.util.ConfigUtil.isValidServer
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.task
import work.msdnicrosoft.avm.util.string.isUuid
import work.msdnicrosoft.avm.util.string.toUuid

object AddCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command = literalCommand("add") {
        requires { hasPermission("avm.command.whitelist.add") }
        wordArgument("player") {
            suggests { builder ->
                buildSet {
                    addAll(PlayerCache.readOnly)
                    addAll(server.allPlayers.map { it.username })
                    addAll(WhitelistManager.usernames)
                }.forEach(builder::suggest)
                builder.buildFuture()
            }
            wordArgument("server") {
                suggests { builder ->
                    val player: String by this
                    val whitelistedServers = if (player.isUuid()) {
                        WhitelistManager.getPlayer(player.toUuid())
                    } else {
                        WhitelistManager.getPlayer(player)
                    }?.serverList
                    buildSet {
                        addAll(config.serverGroups.keys)
                        addAll(server.allServers.map { it.serverInfo.name })
                    }.filterNot {
                        whitelistedServers?.contains(it) == true
                    }.forEach(builder::suggest)
                    builder.buildFuture()
                }
                executes {
                    val player: String by this
                    val server: String by this
                    context.source.addPlayer(player, server)
                    Command.SINGLE_SUCCESS
                }
                boolArgument("onlineMode") {
                    executes {
                        val player: String by this
                        val server: String by this
                        val onlineMode: Boolean by this
                        task { context.source.addPlayer(player, server, onlineMode) }
                        Command.SINGLE_SUCCESS
                    }
                }
            }
        }
    }

    private fun CommandSource.addPlayer(player: String, serverName: String, onlineMode: Boolean? = null) {
        if (!isValidServer(serverName)) {
            this.sendTranslatable("avm.general.not.exist.server", Argument.string("server", serverName))
            return
        }

        val isUuid = player.isUuid()
        if (isUuid && !WhitelistManager.serverIsOnlineMode) {
            this.sendTranslatable("avm.command.avmwl.add.uuid.unsupported")
            return
        }

        val result = if (isUuid) {
            WhitelistManager.add(player.toUuid(), serverName, onlineMode)
        } else {
            WhitelistManager.add(player, serverName, onlineMode)
        }

        val message = when (result) {
            AddResult.SUCCESS -> tr(
                "avm.command.avmwl.add.success",
                Argument.string("player", player),
                Argument.string("server", serverName)
            )

            AddResult.API_LOOKUP_NOT_FOUND -> tr("avm.command.avmwl.add.request.not.found")
            AddResult.API_LOOKUP_REQUEST_FAILED -> tr("avm.command.avmwl.add.request.failed")
            AddResult.ALREADY_EXISTS -> tr("avm.command.avmwl.add.already.exists")
            AddResult.SAVE_FILE_FAILED -> tr("avm.whitelist.save.failed")
        }
        this.sendMessage(message)
    }
}
