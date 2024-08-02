package work.msdnicrosoft.avm.command

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.annotations.ShouldShow
import work.msdnicrosoft.avm.util.command.CommandSessionManager
import work.msdnicrosoft.avm.util.command.CommandUtil
import work.msdnicrosoft.avm.util.command.CommandUtil.buildHelper
import kotlin.system.measureTimeMillis
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

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
