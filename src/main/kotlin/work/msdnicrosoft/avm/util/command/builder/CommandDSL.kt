@file:Suppress("unused")

package work.msdnicrosoft.avm.util.command.builder

import com.mojang.brigadier.arguments.*
import work.msdnicrosoft.avm.util.command.context.CommandContext

@Target(AnnotationTarget.TYPE)
@DslMarker
annotation class CommandDSL

fun Command.stringArgument(name: String, block: @CommandDSL ArgumentCommand<String>.() -> Unit) {
    this.node.then(ArgumentCommand(name, StringArgumentType.string()).apply(block).node)
}

fun Command.wordArgument(name: String, block: @CommandDSL ArgumentCommand<String>.() -> Unit) {
    this.node.then(ArgumentCommand(name, StringArgumentType.word()).apply(block).node)
}

fun Command.greedyStringArgument(name: String, block: @CommandDSL ArgumentCommand<String>.() -> Unit) {
    this.node.then(ArgumentCommand(name, StringArgumentType.greedyString()).apply(block).node)
}

fun Command.boolArgument(name: String, block: @CommandDSL ArgumentCommand<Boolean>.() -> Unit) {
    this.node.then(ArgumentCommand(name, BoolArgumentType.bool()).apply(block).node)
}

fun Command.intArgument(
    name: String,
    min: Int = Int.MIN_VALUE,
    max: Int = Int.MAX_VALUE,
    block: @CommandDSL ArgumentCommand<Int>.() -> Unit
) {
    this.node.then(ArgumentCommand(name, IntegerArgumentType.integer(min, max)).apply(block).node)
}

fun Command.longArgument(
    name: String,
    min: Long = Long.MIN_VALUE,
    max: Long = Long.MAX_VALUE,
    block: @CommandDSL ArgumentCommand<Long>.() -> Unit
) {
    this.node.then(ArgumentCommand(name, LongArgumentType.longArg(min, max)).apply(block).node)
}

fun Command.floatArgument(
    name: String,
    min: Float = Float.MIN_VALUE,
    max: Float = Float.MAX_VALUE,
    block: @CommandDSL ArgumentCommand<Float>.() -> Unit
) {
    this.node.then(ArgumentCommand(name, FloatArgumentType.floatArg(min, max)).apply(block).node)
}

fun Command.doubleArgument(
    name: String,
    min: Double = Double.MIN_VALUE,
    max: Double = Double.MAX_VALUE,
    block: @CommandDSL ArgumentCommand<Double>.() -> Unit
) {
    this.node.then(ArgumentCommand(name, DoubleArgumentType.doubleArg(min, max)).apply(block).node)
}

fun Command.requires(requirement: @CommandDSL S.() -> Boolean) {
    this.node.requires(requirement)
}

fun Command.executes(block: @CommandDSL CommandContext.() -> Int) {
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
