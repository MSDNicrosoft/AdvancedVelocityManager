package work.msdnicrosoft.avm.command.whitelist

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.minimessage.translation.Argument
import taboolib.common.platform.function.submitAsync
import taboolib.common.util.presentRun
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.ConfigUtil.isValidServer
import work.msdnicrosoft.avm.util.ProxyServerUtil.getPlayer
import work.msdnicrosoft.avm.util.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.util.command.getString
import work.msdnicrosoft.avm.util.command.literal
import work.msdnicrosoft.avm.util.command.sendTranslatable
import work.msdnicrosoft.avm.util.command.wordArgument
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
                }.executes { context ->
                    context.source.removePlayer(context.getString("player"))
                    Command.SINGLE_SUCCESS
                }.then(
                    wordArgument("server")
                        .suggests { context, builder ->
                            val player = context.getString("player")
                            val serverList = if (player.isUuid()) {
                                WhitelistManager.getPlayer(player.toUuid())
                            } else {
                                WhitelistManager.getPlayer(player)
                            }?.serverList
                            serverList?.forEach(builder::suggest)
                            builder.buildFuture()
                        }.executes { context ->
                            val serverName = context.getString("server")
                            if (!isValidServer(serverName)) {
                                context.source.sendTranslatable(
                                    "avm.general.not.exist.server",
                                    Argument.string("server", serverName)
                                )
                                return@executes Command.SINGLE_SUCCESS
                            }
                            context.source.removePlayer(context.getString("player"), serverName)
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
