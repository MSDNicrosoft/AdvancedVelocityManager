package work.msdnicrosoft.avm.util.command.brigadier

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource

typealias S = CommandSource

class LiteralCommand(root: String) : Command {
    override val node: LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal(root)

    override fun build(): LiteralCommandNode<S> = node.build()
}

fun literalCommand(literal: String, block: LiteralCommand.() -> Unit) = LiteralCommand(literal).apply(block)
