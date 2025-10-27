package work.msdnicrosoft.avm.util.command.context

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.command.VelocityBrigadierMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.annotations.CommandNode
import work.msdnicrosoft.avm.annotations.RootCommand
import work.msdnicrosoft.avm.util.command.builder.Command
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.component.builder.style.styled
import work.msdnicrosoft.avm.util.component.builder.text.component
import work.msdnicrosoft.avm.util.reflect.getAnnotationIfPresent

@Throws(CommandSyntaxException::class)
fun throwCommandException(message: Component): Nothing =
    throw SimpleCommandExceptionType(VelocityBrigadierMessage.tooltip(message)).create()

@Suppress("UnsafeCallOnNullableType", "SameReturnValue")
fun CommandContext.buildHelp(commandRoot: Class<*>, checkPermission: Boolean = true): Int {
    val rootCommand: RootCommand = commandRoot.getAnnotationIfPresent<RootCommand>() ?: return Command.SINGLE_SUCCESS
    val rootName: String = rootCommand.name

    sendTranslatable("avm.general.help.header.1.text") {
        args {
            component(
                "name",
                tr("avm.general.plugin.name") styled {
                    hoverText { tr("avm.general.help.header.1.name.hover") }
                }
            )
            string("version", plugin.self.version.get())
        }
    }
    sendTranslatable("avm.general.help.header.2.text") {
        args { string("root_command", rootName) }
    }
    sendTranslatable("avm.general.help.header.subcommands")

    commandRoot.resolve()
        .field {
            annotations(CommandNode::class)
        }.map { resolver ->
            resolver.self.getAnnotation(classOf<CommandNode>()) to resolver.get<Command>()!!
        }.forEach { (commandNode, command) ->
            if (checkPermission && !command.node.requirement.test(context.source)) return@forEach

            val arguments: String = commandNode.arguments.joinToString(" ") { arg ->
                when (arg.firstOrNull()) {
                    '[' -> "<dark_gray>$arg"
                    '<' -> "<gray>$arg"
                    else -> arg
                }
            }

            val description: Component = tr("avm.command.$rootName.${commandNode.name}.description")

            sendMessage(JoinConfiguration.spaces()) {
                text("    -") styled { color(NamedTextColor.DARK_GRAY) }
                mini("${commandNode.name} $arguments") styled {
                    color(NamedTextColor.WHITE)
                    hoverText {
                        component(JoinConfiguration.spaces()) {
                            text("$rootName ${commandNode.name}") styled { color(NamedTextColor.WHITE) }
                            text("-") styled { color(NamedTextColor.DARK_GRAY) }
                            componentLike(description) styled { color(NamedTextColor.GRAY) }
                        }
                    }
                    click { suggestCommand("/$rootName ${commandNode.name} $arguments") }
                }
            }
            sendMessage {
                text("      ")
                componentLike(description) styled { color(NamedTextColor.GRAY) }
            }
        }
    return Command.SINGLE_SUCCESS
}
