@file:Suppress("unused", "NOTHING_TO_INLINE")

package work.msdnicrosoft.avm.util.command

import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.commandManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.annotations.CommandNode
import work.msdnicrosoft.avm.annotations.RootCommand
import work.msdnicrosoft.avm.util.command.brigadier.Command
import work.msdnicrosoft.avm.util.command.brigadier.CommandContext
import work.msdnicrosoft.avm.util.component.ComponentUtil
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.reflect.getAnnotationIfPresent
import com.mojang.brigadier.context.CommandContext as BrigadierCommandContext

typealias S = CommandSource

inline val S.isConsole: Boolean
    get() = this is ConsoleCommandSource

inline val S.isPlayer: Boolean
    get() = this is Player

inline val S.name: String
    get() = if (this is Player) this.username else "Console"

inline fun S.toPlayer(): Player = this as Player
inline fun S.toConsole(): ConsoleCommandSource = this as ConsoleCommandSource
inline fun S.toConnectedPlayer(): ConnectedPlayer = this as ConnectedPlayer

inline fun CommandContext<S>.sendMessage(message: Component) = this.context.source.sendMessage(message)

inline fun CommandContext<S>.sendPlainMessage(message: String) = this.context.source.sendPlainMessage(message)

inline fun CommandContext<S>.sendRichMessage(message: String) = this.context.source.sendRichMessage(message)

inline fun CommandContext<S>.sendRichMessage(message: String, vararg resolvers: TagResolver) =
    this.context.source.sendRichMessage(message, *resolvers)

inline fun CommandContext<S>.sendTranslatable(message: String, vararg args: ComponentLike) =
    this.context.source.sendTranslatable(message, *args)

@Suppress("UNCHECKED_CAST", "SameReturnValue")
fun BrigadierCommandContext<CommandSource>.buildHelp(commandRoot: Class<*>, checkPermission: Boolean = true): Int {
    val rootCommand = commandRoot.getAnnotationIfPresent<RootCommand>() ?: return Command.SINGLE_SUCCESS
    val rootName = rootCommand.name

    this.source.sendTranslatable(
        "avm.general.help.header.1.text",
        Argument.component(
            "name",
            tr("avm.general.plugin.name")
                .hoverEvent(HoverEvent.showText(tr("avm.general.help.header.1.name.hover")))
        ),
        Argument.string("version", plugin.self.version.get()),
    )
    this.source.sendTranslatable(
        "avm.general.help.header.2.text",
        Argument.string("root_command", rootName)
    )
    this.source.sendTranslatable("avm.general.help.header.subcommands")

    commandRoot.declaredFields.forEach { field ->
        field.trySetAccessible()

        val commandNode = field.getAnnotationIfPresent<CommandNode>() ?: return@forEach

        val command = field[commandRoot] as Command

        if (checkPermission && !command.node.requirement.test(this.source)) return@forEach

        val arguments = commandNode.arguments.joinToString(" ") { arg ->
            when (arg.firstOrNull()) {
                '[' -> "<dark_gray>$arg"
                '<' -> "<gray>$arg"
                else -> arg
            }
        }

        val description = tr("avm.command.$rootName.${commandNode.name}.description")
        val hoverEvent = HoverEvent.showText(
            ComponentUtil.miniMessage.deserialize(
                "<white>$rootName ${commandNode.name} <dark_gray>- <gray><desc>",
                Placeholder.component("desc", description)
            )
        )
        this.source.sendMessage(
            ComponentUtil.miniMessage.deserialize("    <dark_gray>- <white>${commandNode.name} $arguments")
                .hoverEvent(hoverEvent)
                .clickEvent(ClickEvent.suggestCommand("/$rootName ${commandNode.name}"))
        )
        this.source.sendRichMessage(
            "      <gray><desc>",
            Placeholder.component("desc", description)
        )
    }
    return Command.SINGLE_SUCCESS
}

fun LiteralCommandNode<CommandSource>.register(vararg aliases: String) {
    val command = BrigadierCommand(this)
    val meta = commandManager.metaBuilder(command)
        .aliases(*aliases)
        .plugin(plugin)
        .build()
    commandManager.register(meta, command)
}

fun LiteralCommandNode<CommandSource>.unregister() = commandManager.unregister(this.name)
