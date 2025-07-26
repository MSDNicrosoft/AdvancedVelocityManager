package work.msdnicrosoft.avm.command.whitelist

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.translation.Argument
import taboolib.common.platform.function.submitAsync
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.CommandSessionManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.util.command.literal
import work.msdnicrosoft.avm.util.command.name
import work.msdnicrosoft.avm.util.command.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr

object ClearCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command: LiteralArgumentBuilder<CommandSource> = literal("clear")
        .requires { source -> source.hasPermission("avm.command.whitelist.clear") }
        .executes { context ->
            val sessionId =
                CommandSessionManager.generateSessionId(
                    context.source.name,
                    System.currentTimeMillis(),
                    context.arguments.values.joinToString(" ")
                )

            CommandSessionManager.add(sessionId) {
                if (WhitelistManager.clear()) {
                    context.source.sendTranslatable("avm.command.avmwl.clear.success")
                } else {
                    context.source.sendTranslatable("avm.command.avmwl.clear.failed")
                }
                if (config.enabled) {
                    submitAsync(now = true) {
                        kickPlayers(config.message, plugin.server.allPlayers)
                    }
                }
            }
            context.source.sendTranslatable("avm.command.avmwl.clear.need.confirm.1.text")
            context.source.sendMessage(
                tr(
                    "avm.command.avmwl.clear.need.confirm.2.text",
                    Argument.string("command", "/avm confirm $sessionId")
                )
                    .clickEvent(ClickEvent.runCommand("/avm confirm $sessionId"))
                    .hoverEvent(HoverEvent.showText(tr("avm.command.avmwl.clear.need.confirm.2.hover")))
            )

            Command.SINGLE_SUCCESS
        }
}
