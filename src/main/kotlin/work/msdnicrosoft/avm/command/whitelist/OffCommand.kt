package work.msdnicrosoft.avm.command.whitelist

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.literal
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr

object OffCommand {
    val command: LiteralArgumentBuilder<CommandSource> = literal("off")
        .requires { source -> source.hasPermission("avm.command.whitelist.off") }
        .executes { context ->
            WhitelistManager.enabled = false

            context.source.sendTranslatable(
                "avm.command.avmwl.status.state",
                Argument.component("state", tr("avm.general.off"))
            )

            Command.SINGLE_SUCCESS
        }
}
