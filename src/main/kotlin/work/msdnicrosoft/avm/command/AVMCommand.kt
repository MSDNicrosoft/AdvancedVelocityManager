package work.msdnicrosoft.avm.command

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submitAsync
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.annotations.ShouldShow
import work.msdnicrosoft.avm.command.utility.ImportCommand
import work.msdnicrosoft.avm.command.utility.KickAllCommand
import work.msdnicrosoft.avm.command.utility.KickCommand
import work.msdnicrosoft.avm.command.utility.SendAllCommand
import work.msdnicrosoft.avm.command.utility.SendCommand
import work.msdnicrosoft.avm.module.CommandSessionManager
import work.msdnicrosoft.avm.module.CommandSessionManager.ExecuteResult
import work.msdnicrosoft.avm.util.command.CommandUtil
import work.msdnicrosoft.avm.util.command.CommandUtil.buildHelper
import kotlin.time.measureTime
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@PlatformSide(Platform.VELOCITY)
@CommandHeader(name = "avm")
object AVMCommand {

    @ShouldShow
    @CommandBody(permission = "avm.command.reload")
    val reload = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            var success = false
            val elapsed = measureTime { success = AVM.reload() }
            if (success) {
                sender.sendLang("command-avm-reload-success", elapsed.toString())
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

    @ShouldShow("<session>")
    @CommandBody(permission = "avm.command.confirm")
    val confirm = subCommand {
        dynamic("session") {
            execute<ProxyCommandSender> { sender, context, _ ->
                submitAsync(now = true) {
                    when (CommandSessionManager.executeAction(context["session"])) {
                        ExecuteResult.SUCCESS -> {}
                        ExecuteResult.EXPIRED -> sender.sendLang("command-avm-confirm-expired")
                        ExecuteResult.FAILED -> sender.sendLang("command-avm-confirm-failed")
                        ExecuteResult.NOT_FOUND -> sender.sendLang("command-avm-confirm-not-found")
                    }
                }
            }
        }
    }

    @ShouldShow("<pluginName>", "<defaultServer>")
    @CommandBody(permission = "avm.command.import")
    val import = ImportCommand.command

    @ShouldShow("<player>", "[reason]")
    @CommandBody(permission = "avm.command.kick")
    val kick = KickCommand.command

    @ShouldShow("[server]", "[reason]")
    @CommandBody(permission = "avm.command.kickall")
    val kickall = KickAllCommand.command

    @ShouldShow("<player>", "<server>", "[reason]")
    @CommandBody(permission = "avm.command.send")
    val send = SendCommand.command

    @ShouldShow("<server>", "[reason]")
    @CommandBody(permission = "avm.command.sendall")
    val sendall = SendAllCommand.command

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
        buildHelper(this@AVMCommand.javaClass)
        incorrectCommand(CommandUtil::incorrectCommandFeedback)
        incorrectSender(CommandUtil::incorrectSenderFeedback)
    }
}
