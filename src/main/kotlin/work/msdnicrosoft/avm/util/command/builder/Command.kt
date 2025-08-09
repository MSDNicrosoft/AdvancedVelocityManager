package work.msdnicrosoft.avm.util.command.builder

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.tree.CommandNode
import com.velocitypowered.api.command.CommandSource

typealias S = CommandSource

interface Command {
    val node: ArgumentBuilder<S, *>

    fun build(): CommandNode<S>

    companion object {
        const val SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS
        const val ILLEGAL_ARGUMENT = -2
    }
}
