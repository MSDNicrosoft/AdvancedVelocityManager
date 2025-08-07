package work.msdnicrosoft.avm.util.command.builder

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode

class LiteralCommand(root: String) : Command {
    override val node: LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal(root)

    override fun build(): LiteralCommandNode<S> = node.build()
}

fun literalCommand(literal: String, block: LiteralCommand.() -> Unit) = LiteralCommand(literal).apply(block)
