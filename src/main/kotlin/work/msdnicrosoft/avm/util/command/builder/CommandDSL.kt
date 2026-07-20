@file:Suppress("unused")

package work.msdnicrosoft.avm.util.command.builder

import com.mojang.brigadier.arguments.*
import com.velocitypowered.api.command.CommandSource
import work.msdnicrosoft.avm.util.command.context.CommandContext

fun Command.stringArgument(name: String, block: ArgumentCommand<String>.() -> Unit) =
    argument(name, StringArgumentType.string(), block)

fun Command.wordArgument(name: String, block: ArgumentCommand<String>.() -> Unit) =
    argument(name, StringArgumentType.word(), block)

fun Command.greedyStringArgument(name: String, block: ArgumentCommand<String>.() -> Unit) =
    argument(name, StringArgumentType.greedyString(), block)

fun Command.boolArgument(name: String, block: ArgumentCommand<Boolean>.() -> Unit) =
    argument(name, BoolArgumentType.bool(), block)

fun Command.intArgument(
    name: String,
    min: Int = Int.MIN_VALUE,
    max: Int = Int.MAX_VALUE,
    block: ArgumentCommand<Int>.() -> Unit
) = argument(name, IntegerArgumentType.integer(min, max), block)

fun Command.longArgument(
    name: String,
    min: Long = Long.MIN_VALUE,
    max: Long = Long.MAX_VALUE,
    block: ArgumentCommand<Long>.() -> Unit
) = argument(name, LongArgumentType.longArg(min, max), block)

fun Command.floatArgument(
    name: String,
    min: Float = -Float.MAX_VALUE,
    max: Float = Float.MAX_VALUE,
    block: ArgumentCommand<Float>.() -> Unit
) = argument(name, FloatArgumentType.floatArg(min, max), block)

fun Command.doubleArgument(
    name: String,
    min: Double = -Double.MAX_VALUE,
    max: Double = Double.MAX_VALUE,
    block: ArgumentCommand<Double>.() -> Unit
) = argument(name, DoubleArgumentType.doubleArg(min, max), block)

fun Command.requires(requirement: CommandSource.() -> Boolean) {
    this.node.requires(requirement)
}

fun Command.executes(block: CommandContext.() -> Int) {
    this.node.executes { CommandContext(it).block() }
}

fun Command.then(command: LiteralCommand) {
    this.node.then(command.node)
}

fun <T> Command.then(command: ArgumentCommand<T>) {
    this.node.then(command.node)
}

fun Command.literal(literal: String, block: LiteralCommand.() -> Unit) {
    this.node.then(LiteralCommand(literal).apply(block).node)
}

fun <T> Command.argument(name: String, type: ArgumentType<T>, block: ArgumentCommand<T>.() -> Unit) {
    this.node.then(ArgumentCommand(name, type).apply(block).node)
}
