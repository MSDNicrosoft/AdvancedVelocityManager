package work.msdnicrosoft.avm.util.command.builder

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.ArgumentCommandNode
import work.msdnicrosoft.avm.util.command.context.CommandContext
import java.util.concurrent.CompletableFuture

class ArgumentCommand<T>(root: String, type: ArgumentType<T>) : Command {
    override val node: RequiredArgumentBuilder<S, T> = RequiredArgumentBuilder.argument(root, type)

    override fun build(): ArgumentCommandNode<S, T> = node.build()

    fun suggests(block: CommandContext.(builder: SuggestionsBuilder) -> CompletableFuture<Suggestions>) {
        node.suggests { context, builder ->
            CommandContext(context).block(builder)
        }
    }
}
