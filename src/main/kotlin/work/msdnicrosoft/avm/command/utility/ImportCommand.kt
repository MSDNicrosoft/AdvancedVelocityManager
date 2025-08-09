package work.msdnicrosoft.avm.command.utility

import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.CommandSessionManager
import work.msdnicrosoft.avm.module.imports.PluginName
import work.msdnicrosoft.avm.util.ConfigUtil.isValidServer
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.component.tr
import kotlin.time.measureTimedValue

object ImportCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command = literalCommand("import") {
        requires { hasPermission("avm.command.import") }
        wordArgument("pluginName") {
            suggests { builder ->
                PluginName.plugins.forEach(builder::suggest)
                builder.buildFuture()
            }
            wordArgument("defaultServer") {
                suggests { builder ->
                    config.serverGroups.keys.forEach(builder::suggest)
                    server.allServers.forEach { builder.suggest(it.serverInfo.name) }
                    builder.buildFuture()
                }
                executes {
                    val pluginName: String by this
                    val defaultServer: String by this

                    if (!isValidServer(defaultServer)) {
                        sendTranslatable(
                            "avm.general.not.exist.server",
                            Argument.string("server", defaultServer)
                        )
                        return@executes Command.ILLEGAL_ARGUMENT
                    }

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
