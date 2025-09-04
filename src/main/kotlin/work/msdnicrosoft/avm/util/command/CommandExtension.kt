package work.msdnicrosoft.avm.util.command

import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.command.CommandSource
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.commandManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin

fun LiteralCommandNode<CommandSource>.register(vararg aliases: String) {
    val command = BrigadierCommand(this)
    val meta: CommandMeta = commandManager.metaBuilder(command)
        .aliases(*aliases)
        .plugin(plugin)
        .build()
    commandManager.register(meta, command)
}

fun LiteralCommandNode<CommandSource>.unregister() = commandManager.unregister(this.name)
