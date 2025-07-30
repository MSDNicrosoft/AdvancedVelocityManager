package work.msdnicrosoft.avm.command.whitelist

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.PlayerCache
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.literal
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr

object StatusCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command: LiteralArgumentBuilder<CommandSource> = literal("status")
        .requires { source -> source.hasPermission("avm.command.whitelist.status") }
        .executes { context ->
            val state = if (WhitelistManager.enabled) "on" else "off"

            context.source.sendTranslatable(
                "avm.command.avmwl.list.header",
                Argument.numeric("player", WhitelistManager.size)
            )
            context.source.sendTranslatable(
                "avm.command.avmwl.status.state",
                Argument.component("state", tr("avm.general.$state"))
            )
            context.source.sendTranslatable(
                "avm.command.avmwl.status.cache",
                Argument.numeric("current", PlayerCache.readOnly.size),
                Argument.numeric("total", config.cachePlayers.maxSize)
            )

            Command.SINGLE_SUCCESS
        }
}
