package work.msdnicrosoft.avm.util.command.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.CommandNode
import java.util.concurrent.CompletableFuture

class ArgumentCommand<T>(root: String, type: ArgumentType<T>) : Command {
    override val node: RequiredArgumentBuilder<S, T> = RequiredArgumentBuilder.argument(root, type)

    override fun build(): CommandNode<S> = node.build()
}

fun <T> ArgumentCommand<T>.suggests(
    block: CommandContext<S>.(builder: SuggestionsBuilder) -> CompletableFuture<Suggestions>
) {
    this.node.suggests { context, builder ->
        CommandContext(context).block(builder)
    }
}
