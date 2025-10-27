package work.msdnicrosoft.avm.command.utility

import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.command.session.CommandSessionManager
import work.msdnicrosoft.avm.module.imports.PluginName
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.name
import work.msdnicrosoft.avm.util.command.data.server.Server
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
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
                    server.allServers.forEach { builder.suggest(it.serverInfo.name) }
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
                            PluginName.of(pluginName).import(this, defaultServer.name)
                        }

                        if (success) {
                            sendTranslatable("avm.command.avm.import.success") {
                                args {
                                    string("plugin_name", pluginName)
                                    string("elapsed", elapsed.toString())
                                }
                            }
                        } else {
                            sendTranslatable("avm.command.avm.import.failed") {
                                args { string("plugin_name", pluginName) }
                            }
                        }
                    }
                    sendTranslatable("avm.command.avm.import.need_confirm.1.text")
                    sendMessage {
                        translatable("avm.command.avm.import.need_confirm.2.text") {
                            args { string("command", "/avm confirm $sessionId") }
                        } styled {
                            hoverText { tr("avm.command.avm.import.need_confirm.2.hover") }
                            click { runCommand("/avm confirm $sessionId") }
                        }
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }
}
