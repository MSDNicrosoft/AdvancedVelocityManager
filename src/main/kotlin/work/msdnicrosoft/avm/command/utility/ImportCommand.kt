package work.msdnicrosoft.avm.command.utility

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.CommandSessionManager
import work.msdnicrosoft.avm.module.imports.PluginName
import work.msdnicrosoft.avm.util.ConfigUtil.isValidServer
import work.msdnicrosoft.avm.util.command.*
import work.msdnicrosoft.avm.util.component.tr
import kotlin.time.measureTime

object ImportCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command: LiteralArgumentBuilder<CommandSource> = literal("import")
        .requires { source -> source.hasPermission("avm.command.import") }
        .then(
            wordArgument("Plugin Name")
                .suggests { context, builder ->
                    PluginName.plugins.forEach(builder::suggest)
                    builder.buildFuture()
                }.then(
                    wordArgument("Default Server")
                        .suggests { context, builder ->
                            config.serverGroups.keys.forEach(builder::suggest)
                            server.allServers.forEach { builder.suggest(it.serverInfo.name) }
                            builder.buildFuture()
                        }.executes { context ->
                            val source = context.source

                            val pluginName = context.getString("Plugin Name")
                            val defaultServer = context.getString("Default Server")
                            if (!isValidServer(defaultServer)) {
                                source.sendTranslatable(
                                    "avm.general.not.exist.server",
                                    Argument.string("server", defaultServer)
                                )
                                return@executes Command.SINGLE_SUCCESS
                            }

                            val sessionId = CommandSessionManager.generateSessionId(
                                source.name,
                                System.currentTimeMillis(),
                                context.arguments.values.joinToString(" ")
                            )

                            CommandSessionManager.add(sessionId) {
                                var success = false
                                val elapsed = measureTime {
                                    success = PluginName.of(pluginName).import(source, defaultServer)
                                }

                                if (success) {
                                    source.sendTranslatable(
                                        "avm.command.avm.import.success",
                                        Argument.string("plugin_name", pluginName),
                                        Argument.string("elapsed", elapsed.toString())
                                    )
                                } else {
                                    source.sendTranslatable(
                                        "avm.command.avm.import.failed",
                                        Argument.string("plugin_name", pluginName)
                                    )
                                }
                            }
                            source.sendTranslatable("avm.command.avm.import.need.confirm.1.text")
                            source.sendMessage(
                                tr(
                                    "avm.command.avm.import.need.confirm.2.text",
                                    Argument.string("command", "/avm confirm $sessionId")
                                ).hoverEvent(HoverEvent.showText(tr("avm.command.avm.import.need.confirm.2.hover")))
                            )

                            Command.SINGLE_SUCCESS
                        }
                )
        )
}
