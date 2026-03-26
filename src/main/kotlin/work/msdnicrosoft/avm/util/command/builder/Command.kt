package work.msdnicrosoft.avm.util.command.builder

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.tree.CommandNode
import com.velocitypowered.api.command.CommandSource
import com.mojang.brigadier.Command as MojangCommand

interface Command {
    val node: ArgumentBuilder<CommandSource, *>

    fun build(): CommandNode<CommandSource>

    companion object {
        const val SINGLE_SUCCESS: Int = MojangCommand.SINGLE_SUCCESS
        const val ILLEGAL_ARGUMENT: Int = -2
    }
}
