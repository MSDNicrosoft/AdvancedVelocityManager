package work.msdnicrosoft.avm.command.utility

import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.module.command.session.CommandSessionManager
import work.msdnicrosoft.avm.module.imports.PluginName
import work.msdnicrosoft.avm.util.command.argument.ServerArgumentType
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.component.tr
import kotlin.time.measureTimedValue

object ImportCommand {
    val command = literalCommand("import") {
        requires { hasPermission("avm.command.import") }
        wordArgument("pluginName") {
            suggests { builder ->
                PluginName.plugins.forEach(builder::suggest)
                builder.buildFuture()
            }
            argument("defaultServer", ServerArgumentType.all()) {
                executes {
                    val pluginName: String by this
                    val defaultServer: String by this

                    val sessionId = CommandSessionManager.generateSessionId(
                        context.source.name,
                        System.currentTimeMillis(),
                        context.arguments.values.joinToString(" ")
                    )

                    CommandSessionManager.add(sessionId) {
                        val (success, elapsed) = measureTimedValue {
                            PluginName.of(pluginName).import(context.source, defaultServer)
                        }

                        if (success) {
                            sendTranslatable(
                                "avm.command.avm.import.success",
                                Argument.string("plugin_name", pluginName),
                                Argument.string("elapsed", elapsed.toString())
                            )
                        } else {
                            sendTranslatable(
                                "avm.command.avm.import.failed",
                                Argument.string("plugin_name", pluginName)
                            )
                        }
                    }
                    sendTranslatable("avm.command.avm.import.need.confirm.1.text")
                    sendMessage(
                        tr(
                            "avm.command.avm.import.need.confirm.2.text",
                            Argument.string("command", "/avm confirm $sessionId")
                        ).hoverEvent(HoverEvent.showText(tr("avm.command.avm.import.need.confirm.2.hover")))
                    )
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }
}
