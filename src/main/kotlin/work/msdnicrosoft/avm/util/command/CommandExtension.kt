@file:Suppress("NOTHING_TO_INLINE")

package work.msdnicrosoft.avm.util.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.annotations.CommandNode
import work.msdnicrosoft.avm.annotations.RootCommand
import work.msdnicrosoft.avm.util.component.ComponentUtil
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.reflect.getAnnotationIfPresent

inline val CommandSource.isConsole: Boolean
    get() = this is ConsoleCommandSource

inline val CommandSource.isPlayer: Boolean
    get() = this is Player

inline val CommandSource.name: String
    get() = if (this is Player) this.username else "Console"

inline fun CommandSource.toPlayer(): Player = this as Player
inline fun CommandSource.toConsole(): ConsoleCommandSource = this as ConsoleCommandSource
inline fun CommandSource.toConnectedPlayer(): ConnectedPlayer = this as ConnectedPlayer

@Suppress("UNCHECKED_CAST", "SameReturnValue")
fun CommandContext<CommandSource>.buildHelp(commandRoot: Class<*>, checkPermission: Boolean = true): Int {
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

        val command = field[commandRoot] as LiteralArgumentBuilder<CommandSource>

        if (checkPermission && !command.requirement.test(this.source)) return@forEach

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
                .clickEvent(ClickEvent.suggestCommand("/$rootCommand ${commandNode.name}"))
        )
        this.source.sendMessage(
            ComponentUtil.miniMessage.deserialize(
                "      <gray><desc>",
                Placeholder.component("desc", description)
            )
        )
    }
    return Command.SINGLE_SUCCESS
}
