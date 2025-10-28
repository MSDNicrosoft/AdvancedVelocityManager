package work.msdnicrosoft.avm.util.command.builder

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource

class LiteralCommand(root: String) : Command {
    override val node: LiteralArgumentBuilder<CommandSource> = LiteralArgumentBuilder.literal(root)
    override fun build(): LiteralCommandNode<CommandSource> = this.node.build()
}

fun literalCommand(literal: String, block: LiteralCommand.() -> Unit) = LiteralCommand(literal).apply(block)
