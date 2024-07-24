package work.msdnicrosoft.avm.command

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.command
import taboolib.common.platform.command.player
import taboolib.common.platform.function.submitAsync
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.util.ConfigUtil
import work.msdnicrosoft.avm.util.Extensions.sendMessage
import work.msdnicrosoft.avm.util.ProxyServerUtil
import kotlin.jvm.optionals.getOrElse
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin as AVMPlugin

@PlatformSide(Platform.VELOCITY)
object UtilityCommand {

    @Suppress("unused")
    @Awake(LifeCycle.ENABLE)
    fun setupCommands() {
        sendCommands()
        kickCommands()
    }

    @Suppress("LongMethod")
    fun sendCommands() {
        command("send", permission = "avm.command.send") {
            player("player") {
                suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                    AVMPlugin.server.allPlayers.map { it.username }
                }
                dynamic("server") {
                    suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                        AVMPlugin.server.allServers.map { it.serverInfo.name }
                    }
                    execute<ProxyCommandSender> { sender, context, _ ->
                        val serverName = context["server"]
                        val server = AVMPlugin.server.getServer(serverName).getOrElse {
                            sender.sendLang("server-not-found", serverName)
                            return@execute
                        }
                        val serverNickname = ConfigUtil.getServerNickname(serverName)

                        val playerName = context.player("player").name
                        val player = AVMPlugin.server.getPlayer(playerName).getOrElse {
                            sender.sendLang("player-not-found", playerName)
                            return@execute
                        }

                        ProxyServerUtil.sendPlayer(server, player).thenAccept { success ->
                            if (success) {
                                sender.sendLang(
                                    "send-executor-feedback",
                                    playerName,
                                    serverNickname
                                )
                                context.player("player").sendLang("send-target-feedback", sender.name, serverNickname)
                            } else {
                                sender.sendLang("send-executor-failed", playerName, serverNickname)
                            }
                        }
                    }
                }
            }
        }
        command("sendall", permission = "avm.command.sendall") {
            dynamic("server") {
                suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                    AVMPlugin.server.allServers.map { it.serverInfo.name }
                }
                dynamic("reason") {
                    execute<ProxyCommandSender> { sender, context, _ ->
                        sendAllPlayers(sender, context["server"], context["reason"])
                    }
                }
                execute<ProxyCommandSender> { sender, context, _ ->
                    sendAllPlayers(
                        sender,
                        context["server"],
                        sender.asLangText(
                            "send-target-feedback",
                            sender.name,
                            ConfigUtil.getServerNickname(context["server"])
                        )
                    )
                }
            }
        }
    }

    fun kickCommands() {
        command("kick", permission = "avm.command.kick") {
            dynamic("player") {
                suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                    AVMPlugin.server.allPlayers.map { it.username }
                }
                execute<ProxyCommandSender> { sender, context, _ ->
                    val player = AVMPlugin.server.getPlayer(context["player"]).getOrElse {
                        sender.sendLang("player-not-found", context["player"])
                        return@execute
                    }
                    submitAsync(now = true) {
                        ProxyServerUtil.kickPlayers(sender.asLangText("kick-target-feedback", sender.name), player)
                    }
                }
            }
        }
        command("kickall", permission = "avm.command.kickall") {
            dynamic("server") {
                suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                    AVMPlugin.server.allServers.map { it.serverInfo.name }
                }
                dynamic("reason") {
                    execute<ProxyCommandSender> { sender, context, _ ->
                        kickAllPlayers(sender, context["server"], context["reason"])
                    }
                }
                execute<ProxyCommandSender> { sender, context, _ ->
                    kickAllPlayers(sender, context["server"], sender.asLangText("kick-target-feedback", sender.name))
                }
            }
            execute<ProxyCommandSender> { sender, _, _ ->
                submitAsync(now = true) {
                    ProxyServerUtil.kickPlayers(
                        sender.asLangText("kick-target-feedback", sender.name),
                        AVMPlugin.server.allPlayers.filterNot { it.hasPermission("avm.kickall.bypass") }
                    )
                }
            }
        }
    }

    /**
     * Kicks all players from a specific server.
     *
     * @param sender the command sender
     * @param server the name of the server to kick players from
     * @param reason the reason for the kick
     */
    private fun kickAllPlayers(sender: ProxyCommandSender, server: String, reason: String) {
        val server = AVMPlugin.server.getServer(server).getOrElse {
            sender.sendLang("server-not-found", server)
            return
        }

        val (bypassed, playerToKick) = server.playersConnected
            .partition { it.hasPermission("avm.kickall.bypass") }

        submitAsync(now = true) {
            ProxyServerUtil.kickPlayers(reason, playerToKick)
        }

        sender.sendLang("kickall-executor-feedback", playerToKick.size, bypassed.size)
    }

    /**
     * Sends all players to a specific server.
     *
     * @param sender the command sender
     * @param serverName the name of the server to send players to
     * @param reason the reason for the send
     */
    private fun sendAllPlayers(sender: ProxyCommandSender, serverName: String, reason: String) {
        val server = AVMPlugin.server.getServer(serverName).getOrElse {
            sender.sendLang("server-not-found", serverName)
            return
        }
        val serverNickname = ConfigUtil.getServerNickname(serverName)

        val (bypassed, playerToSend) = AVMPlugin.server.allPlayers
            .filterNot { it.currentServer.get().serverInfo.name == serverName }
            .partition { it.hasPermission("avm.sendall.bypass") }

        val failedPlayers = buildList {
            playerToSend.forEach { player ->
                ProxyServerUtil.sendPlayer(server, player).thenAccept { success ->
                    if (success) {
                        player.sendMessage(reason)
                    } else {
                        add(player)
                    }
                }
            }
        }
        sender.sendLang(
            "sendall-executor-feedback",
            playerToSend - failedPlayers.size,
            serverNickname,
            bypassed.size,
            failedPlayers.size
        )
    }
}
