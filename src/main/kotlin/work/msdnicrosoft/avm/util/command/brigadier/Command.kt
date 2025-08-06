package work.msdnicrosoft.avm.util.command.brigadier

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.tree.CommandNode

interface Command {
    val node: ArgumentBuilder<S, *>

    fun build(): CommandNode<S> = node.build()

    companion object {
        const val SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS
    }
}
