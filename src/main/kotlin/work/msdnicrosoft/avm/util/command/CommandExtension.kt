@file:Suppress("NOTHING_TO_INLINE")

package work.msdnicrosoft.avm.util.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin

inline fun literal(literal: String): LiteralArgumentBuilder<CommandSource> = literal(literal)

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

inline fun CommandContext<CommandSource>.getString(name: String): String =
    StringArgumentType.getString(this, name)

inline fun CommandContext<CommandSource>.getInt(name: String): Int =
    IntegerArgumentType.getInteger(this, name)

inline fun CommandContext<CommandSource>.getBool(name: String): Boolean =
    BoolArgumentType.getBool(this, name)

inline val CommandSource.isConsole: Boolean
    get() = this is ConsoleCommandSource

inline val CommandSource.isPlayer: Boolean
    get() = this is Player

inline val CommandSource.name: String
    get() = if (this is Player) this.username else "Console"

inline fun CommandSource.toPlayer() = this as Player
inline fun CommandSource.toConsole() = this as ConsoleCommandSource
inline fun CommandSource.toConnectedPlayer(): ConnectedPlayer = this as ConnectedPlayer

inline fun CommandSource.sendTranslatable(
    key: String,
    vararg args: ComponentLike
) = this.sendMessage(Component.translatable(key, *args))

fun LiteralCommandNode<CommandSource>.register(vararg aliases: String) {
    val command = BrigadierCommand(this)
    val meta = plugin.server.commandManager.metaBuilder(command)
        .aliases(*aliases)
        .plugin(plugin)
        .build()
    plugin.server.commandManager.register(meta, command)
}

fun LiteralCommandNode<CommandSource>.unregister() =
    plugin.server.commandManager.unregister(this.name)
