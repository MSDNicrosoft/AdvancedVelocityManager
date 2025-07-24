package work.msdnicrosoft.avm.command.utility

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.subCommand
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.CommandSessionManager
import work.msdnicrosoft.avm.module.imports.PluginName
import work.msdnicrosoft.avm.util.ConfigUtil.isValidServer
import kotlin.time.measureTime
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@PlatformSide(Platform.VELOCITY)
object ImportCommand {

    private inline val config
        get() = ConfigManager.config.whitelist

    val command = subCommand {
        dynamic("pluginName") {
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ -> PluginName.plugins }
            dynamic("defaultServer") {
                suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                    (config.serverGroups.keys + AVM.plugin.server.allServers.map { it.serverInfo.name }).toList()
                }
                execute<ProxyCommandSender> { sender, context, argument ->
                    val pluginName = context["pluginName"]
                    val defaultServer = context["defaultServer"]
                    if (!isValidServer(defaultServer)) {
                        sender.sendLang("server-not-found", defaultServer)
                        return@execute
                    }
                    val sessionId = CommandSessionManager.generateSessionId(
                        sender.name,
                        System.currentTimeMillis(),
                        argument
                    )
                    CommandSessionManager.add(sessionId) {
                        var success = false
                        val elapsed = measureTime {
                            success = PluginName.of(pluginName).import(sender, defaultServer)
                        }

                        if (success) {
                            sender.sendLang("command-avm-import-success", pluginName, elapsed.toString())
                        } else {
                            sender.sendLang("command-avm-import-failed", pluginName)
                        }
                    }
                    sender.sendLang("command-avm-import-need-confirm", "/avm confirm $sessionId")
                }
            }
        }
    }
}
