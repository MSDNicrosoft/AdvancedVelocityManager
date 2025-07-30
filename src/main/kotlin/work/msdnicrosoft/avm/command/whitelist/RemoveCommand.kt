package work.msdnicrosoft.avm.command.whitelist

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.ConfigUtil.isValidServer
import work.msdnicrosoft.avm.util.command.*
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.server.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.util.server.task
import work.msdnicrosoft.avm.util.string.isUuid
import work.msdnicrosoft.avm.util.string.toUuid

object RemoveCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command: LiteralArgumentBuilder<CommandSource> = literal("remove")
        .requires { source -> source.hasPermission("avm.command.whitelist.remove") }
        .then(
            wordArgument("player")
                .suggests { context, builder ->
                    WhitelistManager.usernames.forEach(builder::suggest)
                    builder.buildFuture()
                }
                .executes { context ->
                    context.source.removePlayer(context.get<String>("player"))
                    Command.SINGLE_SUCCESS
                }
                .then(
                    wordArgument("server")
                        .suggests { context, builder ->
                            val player = context.get<String>("player")
                            val serverList = if (player.isUuid()) {
                                WhitelistManager.getPlayer(player.toUuid())
                            } else {
                                WhitelistManager.getPlayer(player)
                            }?.serverList
                            serverList?.forEach(builder::suggest)
                            builder.buildFuture()
                        }
                        .executes { context ->
                            val serverName = context.get<String>("server")
                            if (!isValidServer(serverName)) {
                                context.source.sendTranslatable(
                                    "avm.general.not.exist.server",
                                    Argument.string("server", serverName)
                                )
                                return@executes Command.SINGLE_SUCCESS
                            }
                            context.source.removePlayer(context.get<String>("player"), serverName)
                            Command.SINGLE_SUCCESS
                        }
                )
        )

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

        when (result) {
            WhitelistManager.RemoveResult.SUCCESS -> {
                if (serverName != null) {
                    sendTranslatable(
                        "avm.command.avmwl.remove.success.server",
                        Argument.string("server", serverName),
                        Argument.string("player", playerName)
                    )
                } else {
                    sendTranslatable(
                        "avm.command.avmwl.remove.success.full",
                        Argument.string("player", playerName)
                    )
                }
            }

            WhitelistManager.RemoveResult.FAIL_NOT_FOUND -> sendTranslatable("avm.command.avmwl.remove.not.found")
            WhitelistManager.RemoveResult.SAVE_FILE_FAILED -> sendTranslatable("avm.whitelist.save.failed")
        }

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
