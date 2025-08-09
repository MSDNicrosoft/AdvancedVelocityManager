@file:Suppress("unused", "NOTHING_TO_INLINE")

package work.msdnicrosoft.avm.util.command.context

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.annotations.CommandNode
import work.msdnicrosoft.avm.annotations.RootCommand
import work.msdnicrosoft.avm.util.command.builder.Command
import work.msdnicrosoft.avm.util.component.ComponentUtil
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.reflect.getAnnotationIfPresent

@Suppress("UnsafeCallOnNullableType", "SameReturnValue")
fun CommandContext.buildHelp(commandRoot: Class<*>, checkPermission: Boolean = true): Int {
    val rootCommand = commandRoot.getAnnotationIfPresent<RootCommand>() ?: return Command.SINGLE_SUCCESS
    val rootName = rootCommand.name

    sendTranslatable(
        "avm.general.help.header.1.text",
        Argument.component(
            "name",
            tr("avm.general.plugin.name")
                .hoverEvent(HoverEvent.showText(tr("avm.general.help.header.1.name.hover")))
        ),
        Argument.string("version", plugin.self.version.get()),
    )
    sendTranslatable(
        "avm.general.help.header.2.text",
        Argument.string("root_command", rootName)
    )
    sendTranslatable("avm.general.help.header.subcommands")

    commandRoot.resolve()
        .field {
            annotations(CommandNode::class)
        }.map { resolver ->
            resolver.self.getAnnotation(classOf<CommandNode>()) to resolver.get<Command>()!!
        }.forEach { (commandNode, command) ->
            if (checkPermission && !command.node.requirement.test(context.source)) return@forEach

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
            sendMessage(
                ComponentUtil.miniMessage.deserialize("    <dark_gray>- <white>${commandNode.name} $arguments")
                    .hoverEvent(hoverEvent)
                    .clickEvent(ClickEvent.suggestCommand("/$rootName ${commandNode.name}"))
            )
            sendRichMessage("      <gray><desc>", Placeholder.component("desc", description))
        }
    return Command.SINGLE_SUCCESS
}
