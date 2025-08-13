package work.msdnicrosoft.avm.command.utility

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.command.argument.ComponentArgumentType
import work.msdnicrosoft.avm.util.command.argument.PlayerArgumentType
import work.msdnicrosoft.avm.util.command.argument.ServerArgumentType
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.CommandContext
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.sendToServer

object SendCommand {
    val command = literalCommand("send") {
        requires { hasPermission("avm.command.send") }
        argument("player", PlayerArgumentType.name()) {
            argument("server", ServerArgumentType.registered()) {
                executes {
                    val server: String by this
                    val player: Player by this
                    sendPlayer(
                        player,
                        server,
                        tr(
                            "avm.command.avm.send.target",
                            Argument.string("executor", context.source.name),
                            Argument.string("server", getServerNickname(server))
                        )
                    )
                    Command.SINGLE_SUCCESS
                }
                argument("reason", ComponentArgumentType.miniMessage()) {
                    executes {
                        val server: String by this
                        val player: Player by this
                        val reason: Component by this
                        sendPlayer(player, server, reason)
                        Command.SINGLE_SUCCESS
                    }
                }
            }
        }
    }

    private fun CommandContext.sendPlayer(player: Player, serverName: String, reason: Component) {
        val registeredServer = server.getServer(serverName).get()
        val serverNickname = getServerNickname(serverName)

        player.sendToServer(registeredServer).thenAccept { success ->
            if (success) {
                this.sendTranslatable(
                    "avm.command.avm.send.executor.success",
                    Argument.string("player", player.name),
                    Argument.string("server", serverNickname)
                )
                player.sendMessage(reason)
            } else {
                this.sendTranslatable(
                    "avm.command.avm.send.executor.failed",
                    Argument.string("player", player.name),
                    Argument.string("server", serverNickname)
                )
            }
        }
    }
}
