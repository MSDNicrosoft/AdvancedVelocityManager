@file:Suppress("NOTHING_TO_INLINE")

package work.msdnicrosoft.avm.util.command

import com.highcapable.kavaref.extension.classOf
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.commandManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin

inline fun literal(literal: String): LiteralArgumentBuilder<CommandSource> = LiteralArgumentBuilder.literal(literal)

inline fun <T> argument(name: String, type: ArgumentType<T>): RequiredArgumentBuilder<CommandSource, T> =
    RequiredArgumentBuilder.argument(name, type)

inline fun intArgument(name: String): RequiredArgumentBuilder<CommandSource, Int> =
    argument(name, IntegerArgumentType.integer())

inline fun wordArgument(name: String): RequiredArgumentBuilder<CommandSource, String> =
    argument(name, StringArgumentType.word())

inline fun greedyStringArgument(name: String): RequiredArgumentBuilder<CommandSource, String> =
    argument(name, StringArgumentType.greedyString())

inline fun boolArgument(name: String): RequiredArgumentBuilder<CommandSource, Boolean> =
    argument(name, BoolArgumentType.bool())

inline fun <reified T : Any> CommandContext<CommandSource>.get(name: String): T = this.getArgument(name, classOf<T>())

fun LiteralCommandNode<CommandSource>.register(vararg aliases: String) {
    val command = BrigadierCommand(this)
    val meta = commandManager.metaBuilder(command)
        .aliases(*aliases)
        .plugin(plugin)
        .build()
    commandManager.register(meta, command)
}

fun LiteralCommandNode<CommandSource>.unregister() = commandManager.unregister(this.name)
