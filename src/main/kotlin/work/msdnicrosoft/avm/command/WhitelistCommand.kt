package work.msdnicrosoft.avm.command

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.function.submitAsync
import taboolib.common.util.isConsole
import taboolib.common.util.presentRun
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.annotations.ShouldShow
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager.AddResult
import work.msdnicrosoft.avm.module.whitelist.WhitelistPlayer
import work.msdnicrosoft.avm.util.ConfigUtil.isServerGroupName
import work.msdnicrosoft.avm.util.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.util.StringUtil.isUuid
import work.msdnicrosoft.avm.util.StringUtil.toUuid
import work.msdnicrosoft.avm.util.command.CommandSessionManager
import work.msdnicrosoft.avm.util.command.CommandUtil.buildHelper
import work.msdnicrosoft.avm.util.command.PageTurner
import kotlin.jvm.optionals.getOrNull
import kotlin.math.ceil
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@Suppress("unused")
@PlatformSide(Platform.VELOCITY)
@CommandHeader(name = "avmwl")
object WhitelistCommand {

    val config
        get() = AVM.config.whitelist

    @ShouldShow("[page]")
    @CommandBody(permission = "avm.command.whitelist.list")
    val list = subCommand {
        int("page") {
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ ->
                (1..WhitelistManager.maxPage).map { it.toString() }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                listWhitelist(sender, context.int("page"))
            }
        }
        execute<ProxyCommandSender> { sender, _, _ ->
            listWhitelist(sender, 1)
        }
    }

    @ShouldShow("<player>", "<server>")
    @CommandBody(permission = "avm.command.whitelist.add")
    val add = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ ->
                buildSet {
                    addAll(PlayerCache.players)
                    addAll(AVM.plugin.server.allPlayers.map { it.username })
                    addAll(WhitelistManager.getWhitelist().map { it.name })
                }.toList()
            }
            dynamic("server") {
                suggestion<ProxyCommandSender>(uncheck = true) { _, context ->
                    val player = context["player"]
                    val whitelistedServers = if (player.isUuid()) {
                        WhitelistManager.getPlayer(player.toUuid())
                    } else {
                        WhitelistManager.getPlayer(player)
                    }?.serverList
                    buildList {
                        addAll(config.serverGroups.keys.filter { whitelistedServers?.contains(it) != true })
                        addAll(
                            AVM.plugin.server.allServers.map { it.serverInfo.name }
                                .filter { whitelistedServers?.contains(it) != true }
                        )
                    }
                }
                execute<ProxyCommandSender> { sender, context, _ ->
                    val serverName = context["server"]
                    if (AVM.plugin.server.getServer(serverName).isEmpty && !isServerGroupName(serverName)) {
                        sender.sendLang("server-not-found", serverName)
                        return@execute
                    }

                    val player = context["player"]
                    val isUuid = player.isUuid()
                    if (isUuid && !WhitelistManager.serverIsOnlineMode) {
                        sender.sendLang("command-avmwl-add-uuid-supported")
                    }

                    val result = if (isUuid) {
                        WhitelistManager.add(player.toUuid(), serverName)
                    } else {
                        WhitelistManager.add(player, serverName)
                    }

                    when (result) {
                        AddResult.SUCCESS -> sender.sendLang("command-avmwl-add-success", serverName, player)
                        AddResult.API_LOOKUP_NOT_FOUND -> sender.sendLang("command-avmwl-add-request-not-found")
                        AddResult.API_LOOKUP_REQUEST_FAILED -> sender.sendLang("command-avmwl-add-request-failed")
                        AddResult.ALREADY_EXISTS -> sender.sendLang("command-avmwl-add-already-exists")
                        AddResult.SAVE_FILE_FAILED -> sender.sendLang("command-avmwl-save-failed")
                    }
                }
            }
        }
    }

    @ShouldShow("<player>", "[server]")
    @CommandBody(permission = "avm.command.whitelist.remove")
    val remove = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                WhitelistManager.getWhitelist().map { it.name }
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
                    if (AVM.plugin.server.getServer(serverName).isEmpty && !isServerGroupName(serverName)) {
                        sender.sendLang("server-not-found", serverName)
                        return@execute
                    }
                    removePlayer(context["player"], context["server"], sender)
                }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                removePlayer(context["player"], null, sender)
            }
        }
    }

    @ShouldShow
    @CommandBody(permission = "avm.command.whitelist.clear")
    val clear = subCommand {
        execute<ProxyCommandSender> { sender, context, argument ->
            val sessionId = CommandSessionManager.generateSessionId(sender.name, System.currentTimeMillis(), argument)

            CommandSessionManager.add(sessionId) {
                if (WhitelistManager.clear()) {
                    sender.sendLang("command-avmwl-clear-success")
                } else {
                    sender.sendLang("command-avmwl-clear-failed")
                }
                if (config.enabled) {
                    submitAsync(now = true) {
                        kickPlayers(config.message, AVM.plugin.server.allPlayers)
                    }
                }
            }
            sender.sendLang("command-avmwl-clear-need-confirm", "/avm confirm $sessionId")
        }
    }

    @ShouldShow("<keywords>", "[page]")
    @CommandBody(permission = "avm.command.whitelist.find")
    val find = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ ->
                buildList {
                    addAll(WhitelistManager.getWhitelist().map { it.name })
                    addAll(PlayerCache.players)
                }
            }
            int("page") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    listFind(sender, context.int("page"), context["player"])
                }
            }
            execute<ProxyCommandSender> { sender, context, _ ->
                listFind(sender, 1, context["player"])
            }
        }
    }

    @ShouldShow
    @CommandBody(permission = "avm.command.whitelist.on")
    val on = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            WhitelistManager.state = WhitelistManager.WhitelistState.ON
            val whitelistAsUuid = WhitelistManager.getWhitelist().map { it.uuid }

            AVM.saveConfig()

            submitAsync(now = true) {
                kickPlayers(
                    config.message,
                    AVM.plugin.server.allPlayers.let { players ->
                        if (WhitelistManager.whitelistSize == 0) {
                            players
                        } else {
                            players.filter { it.uniqueId !in whitelistAsUuid }
                        }
                    }
                )
            }

            sender.sendLang("command-avmwl-state", sender.asLangText("general-on"))
        }
    }

    @ShouldShow
    @CommandBody(permission = "avm.command.whitelist.off")
    val off = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            WhitelistManager.state = WhitelistManager.WhitelistState.OFF
            AVM.saveConfig()
            sender.sendLang("command-avmwl-state", sender.asLangText("general-off"))
        }
    }

    @ShouldShow
    @CommandBody(permission = "avm.command.whitelist.status")
    val status = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            val whitelistState = WhitelistManager.state.name.lowercase()
            sender.sendLang("command-avmwl-state", sender.asLangText("general-$whitelistState"))
            sender.sendLang("command-avmwl-list-header", WhitelistManager.whitelistSize)
            sender.sendLang("command-avmwl-status-cache", PlayerCache.players.size, config.cachePlayers.maxSize)
        }
    }

    @CommandBody
    val main = mainCommand {
        buildHelper(this@WhitelistCommand::class)
    }

    private fun removePlayer(player: String, serverName: String?, sender: ProxyCommandSender) {
        val isUuid = player.isUuid()
        val result = if (isUuid) {
            WhitelistManager.remove(player.toUuid(), serverName)
        } else {
            WhitelistManager.remove(player, serverName)
        }

        when (result) {
            WhitelistManager.RemoveResult.SUCCESS -> {
                if (serverName != null) {
                    sender.sendLang("command-avmwl-remove-server-success", serverName, player)
                } else {
                    sender.sendLang("command-avmwl-remove-full-success", player)
                }
            }

            WhitelistManager.RemoveResult.FAIL_NOT_FOUND -> sender.sendLang("command-avmwl-remove-not-found")
            WhitelistManager.RemoveResult.SAVE_FILE_FAILED -> sender.sendLang("command-avmwl-save-failed")
        }

        if (config.enabled) {
            submitAsync(now = true) {
                with(AVM.plugin.server) {
                    if (isUuid) getPlayer(player.toUuid()) else getPlayer(player)
                }.presentRun {
                    if (!WhitelistManager.isInServerWhitelist(uniqueId, currentServer.get().serverInfo.name)) {
                        kickPlayers(config.message, this)
                    }
                }
            }
        }
    }

    /**
     * Sends a message to the sender with the header for the whitelist list,
     * then sends a message for each player on the specified page.
     * Finally, sends a message with the footer for the whitelist list.
     *
     * @param sender The sender of the command.
     * @param page The page number to retrieve.
     */
    private fun listWhitelist(sender: ProxyCommandSender, page: Int) {
        if (page < 1) {
            sender.sendLang("whitelist-page-must-larger-than-zero")
            return
        }
        if (WhitelistManager.whitelistIsEmpty || page > WhitelistManager.maxPage) {
            sender.sendLang("command-avmwl-list-empty")
            return
        }

        if (page == 1) {
            sender.sendLang("command-avmwl-list-header", WhitelistManager.whitelistSize)
        }
        val senderAsPlayer = AVM.plugin.server.getPlayer(sender.name).getOrNull()
        WhitelistManager.getPagedWhitelist(page).forEach { player ->
            if (sender.isConsole()) {
                sender.sendMessage("&7${player.name} &8:&r ${player.serverList.joinToString()}")
            } else {
                senderAsPlayer?.sendMessage(WhitelistPlayer(player, sender).build())
            }
        }

        PageTurner(sender, "/avmwl list")
            .build(page, WhitelistManager.maxPage)
            .sendTo(sender)
    }

    /**
     * Sends a message to the sender with the header for the whitelist find,
     * then sends a message for each player found on the specified page.
     * Finally, sends a message with the footer for the whitelist find.
     *
     * @param sender The sender of the command.
     * @param page The page number to retrieve.
     * @param player The username to search for.
     */
    private fun listFind(sender: ProxyCommandSender, page: Int, player: String) {
        if (page < 1) {
            sender.sendLang("whitelist-page-must-larger-than-zero")
            return
        }

        val result = WhitelistManager.find(player, page)
        val maxPage = ceil(result.size.toInt() / 10F).toInt()

        if (result.isNotEmpty() || page <= maxPage) {
            if (page == 1) {
                sender.sendLang("command-avmwl-find-header")
            }
            val senderAsPlayer = AVM.plugin.server.getPlayer(sender.name).getOrNull()
            result.forEach { player ->
                if (sender.isConsole()) {
                    sender.sendMessage("&7${player.name} &8:&r ${player.serverList.joinToString()}")
                } else {
                    senderAsPlayer?.sendMessage(WhitelistPlayer(player, sender).build())
                }
            }
            PageTurner(sender, "/avmwl find $player")
                .build(page, maxPage)
                .sendTo(sender)
        }
    }
}
