package work.msdnicrosoft.avm.command.utility

import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.command.session.CommandSessionManager
import work.msdnicrosoft.avm.module.imports.PluginName
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.command.data.server.Server
import work.msdnicrosoft.avm.util.component.clickToRunCommand
import work.msdnicrosoft.avm.util.component.hoverText
import work.msdnicrosoft.avm.util.component.tr
import kotlin.time.Duration
import kotlin.time.measureTimedValue

object ImportCommand {
    val command = literalCommand("import") {
        requires { hasPermission("avm.command.import") }
        wordArgument("pluginName") {
            suggests { builder ->
                PluginName.PLUGINS.forEach(builder::suggest)
                builder.buildFuture()
            }
            wordArgument("defaultServer") {
                suggests { builder ->
                    server.allServers.map { it.serverInfo.name }.forEach(builder::suggest)
                    ConfigManager.config.whitelist.serverGroups.keys.forEach(builder::suggest)
                    builder.buildFuture()
                }
                executes {
                    val pluginName: String by this
                    val defaultServer: Server by this

                    val sessionId: String = CommandSessionManager.generateSessionId(
                        context.source.name,
                        System.currentTimeMillis(),
                        context.arguments.values.joinToString(" ")
                    )

                    CommandSessionManager.add(sessionId) {
                        val (success: Boolean, elapsed: Duration) = measureTimedValue {
                            PluginName.of(pluginName).import(context.source, defaultServer.name)
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
                        ).hoverText(tr("avm.command.avm.import.need.confirm.2.hover"))
                            .clickToRunCommand("/avm confirm $sessionId")
                    )
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }
}
