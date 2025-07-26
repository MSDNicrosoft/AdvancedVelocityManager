package work.msdnicrosoft.avm.command.whitelist

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.minimessage.translation.Argument
import taboolib.common.platform.function.submitAsync
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.ProxyServerUtil.kickPlayers
import work.msdnicrosoft.avm.util.command.literal
import work.msdnicrosoft.avm.util.command.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr

object OnCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command: LiteralArgumentBuilder<CommandSource> = literal("on")
        .requires { source -> source.hasPermission("avm.command.whitelist.on") }
        .executes { context ->
            WhitelistManager.enabled = true

            context.source.sendTranslatable(
                "avm.command.avmwl.status.state",
                Argument.component("state", tr("avm.general.on"))
            )

            submitAsync(now = true) {
                kickPlayers(
                    config.message,
                    if (WhitelistManager.isEmpty) {
                        plugin.server.allPlayers
                    } else {
                        plugin.server.allPlayers.filter { it.uniqueId !in WhitelistManager.uuids }
                    }
                )
            }

            Command.SINGLE_SUCCESS
        }
}
