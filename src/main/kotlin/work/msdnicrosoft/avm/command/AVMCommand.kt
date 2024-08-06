package work.msdnicrosoft.avm.command

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.annotations.ShouldShow
import work.msdnicrosoft.avm.util.ConfigUtil.isServerGroupName
import work.msdnicrosoft.avm.util.command.CommandSessionManager
import work.msdnicrosoft.avm.util.command.CommandUtil
import work.msdnicrosoft.avm.util.command.CommandUtil.buildHelper
import kotlin.system.measureTimeMillis
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM
import work.msdnicrosoft.avm.util.importer.LlsManagerUtil.import as LlsManagerImport
import work.msdnicrosoft.avm.util.importer.QuAnVelocityWhitelistUtil.import as QuAnVelocityWhitelistImport

@Suppress("unused")
@PlatformSide(Platform.VELOCITY)
@CommandHeader(name = "avm")
object AVMCommand {

    @ShouldShow
    @CommandBody(permission = "avm.command.reload")
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            var success = false
            val elapsed = measureTimeMillis { success = AVM.reload() }
            if (success) {
                sender.sendLang("command-avm-reload-success", elapsed)
            } else {
                sender.sendLang("command-avm-reload-failed")
            }
        }
    }

    @ShouldShow
    @CommandBody(permission = "avm.command.info")
    val info = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            val velocity = AVM.plugin.server.version
            // TODO Enabled & Disabled modules
            sender.sendLang(
                "command-avm-info",
                AVM.self.name.get(),
                AVM.self.version.get(),
                "${velocity.name} ${velocity.version}"
            )
        }
    }

    @ShouldShow
    @CommandBody(permission = "avm.command.confirm")
    val confirm = subCommand {
        dynamic("session") {
            execute<ProxyCommandSender> { sender, context, _ ->
                when (CommandSessionManager.executeAction(context["session"])) {
                    CommandSessionManager.ExecuteResult.SUCCESS -> {}
                    CommandSessionManager.ExecuteResult.EXPIRED -> sender.sendLang("command-avm-confirm-expired")
                    CommandSessionManager.ExecuteResult.FAILED -> sender.sendLang("command-avm-confirm-failed")
                    CommandSessionManager.ExecuteResult.NOT_FOUND -> sender.sendLang("command-avm-confirm-not-found")
                }
            }
        }
    }

    enum class PluginName { LLS_MANAGER, QU_AN_VELOCITYWHITELIST }

    @ShouldShow
    @CommandBody(permission = "avm.command.import")
    val import = subCommand {
        dynamic("pluginName") {
            suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                listOf("lls-manager", "qu-an-velocitywhitelist")
            }
            dynamic("defaultServer") {
                suggestion<ProxyCommandSender>(uncheck = false) { _, _ ->
                    buildSet {
                        addAll(AVM.config.whitelist.serverGroups.keys)
                        addAll(AVM.plugin.server.allServers.map { it.serverInfo.name })
                    }.toList()
                }
                execute<ProxyCommandSender> { sender, context, argument ->
                    val pluginName = context["pluginName"]
                    val defaultServer = context["defaultServer"]
                    if (AVM.plugin.server.getServer(defaultServer).isEmpty && !isServerGroupName(defaultServer)) {
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
                        val elapsed = measureTimeMillis {
                            success = when (PluginName.valueOf(pluginName.replace("-", "_").uppercase())) {
                                PluginName.LLS_MANAGER -> sender.LlsManagerImport(defaultServer)
                                PluginName.QU_AN_VELOCITYWHITELIST -> sender.QuAnVelocityWhitelistImport(defaultServer)
                            }
                        }
                        if (success) {
                            sender.sendLang("command-avm-import-success", pluginName, elapsed)
                        } else {
                            sender.sendLang("command-avm-import-failed", pluginName)
                        }
                    }
                    sender.sendLang("command-avm-import-need-confirm", "/avm confirm $sessionId")
                }
            }
        }
    }

//    @ShouldShow
//    @CommandBody(permission = "avm.command.enable")
//    val enable = subCommand {
//        dynamic("feature") {
//
//        }
//    }
//
//    @ShouldShow
//    @CommandBody(permission = "avm.command.disable")
//    val disable = subCommand {
//        dynamic("feature") {
//            suggestion<ProxyCommandSender>(uncheck = true) { sender, context ->
//
//            }
//        }
//    }

    @CommandBody
    val main = mainCommand {
        buildHelper(this@AVMCommand::class)
        incorrectCommand(CommandUtil::incorrectCommandFeedback)
        incorrectSender(CommandUtil::incorrectSenderFeedback)
    }
}
