package work.msdnicrosoft.avm.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.annotations.CommandNode
import work.msdnicrosoft.avm.annotations.RootCommand
import work.msdnicrosoft.avm.command.utility.*
import work.msdnicrosoft.avm.module.CommandSessionManager
import work.msdnicrosoft.avm.module.CommandSessionManager.ExecuteResult
import work.msdnicrosoft.avm.util.command.*
import work.msdnicrosoft.avm.util.component.sendTranslatable
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.server.task
import kotlin.time.measureTimedValue

@RootCommand("avm")
object AVMCommand {

    fun init() {
        command.register()
    }

    fun disable() {
        command.unregister()
    }

    @CommandNode("reload")
    val reload: LiteralArgumentBuilder<CommandSource> = literal("reload")
        .requires { source -> source.hasPermission("avm.command.reload") }
        .executes { context ->
            val (success, elapsed) = measureTimedValue { plugin.reload() }
            if (success) {
                context.source.sendTranslatable(
                    "avm.command.avm.reload.success",
                    Argument.string("elapsed", elapsed.inWholeMilliseconds.toString())
                )
            } else {
                context.source.sendTranslatable("avm.command.avm.reload.failed")
            }

            Command.SINGLE_SUCCESS
        }

    @CommandNode("info")
    val info: LiteralArgumentBuilder<CommandSource> = literal("info")
        .requires { source -> source.hasPermission("avm.command.info") }
        .executes { context ->
            val velocity = plugin.server.version
            // TODO Enabled & Disabled modules
            context.source.sendTranslatable(
                "avm.command.avm.info.plugin.name",
                Argument.component("name", tr("avm.general.plugin.name"))
            )
            context.source.sendTranslatable(
                "avm.command.avm.info.plugin.version",
                Argument.string("version", plugin.self.version.get())
            )
            context.source.sendTranslatable(
                "avm.command.avm.info.server",
                Argument.string("server", "${velocity.name} ${velocity.version}")
            )
            Command.SINGLE_SUCCESS
        }

    @CommandNode("confirm", "<session>")
    val confirm: LiteralArgumentBuilder<CommandSource> = literal("confirm")
        .requires { source -> source.hasPermission("avm.command.confirm") }
        .then(
            wordArgument("session")
                .executes { context ->
                    task {
                        val result = when (CommandSessionManager.executeAction(context.get<String>("session"))) {
                            ExecuteResult.SUCCESS -> return@task
                            ExecuteResult.EXPIRED -> "avm.command.avm.confirm.expired"
                            ExecuteResult.FAILED -> "avm.command.avm.confirm.failed"
                            ExecuteResult.NOT_FOUND -> "avm.command.avm.confirm.not.found"
                        }
                        context.source.sendTranslatable(result)
                    }
                    Command.SINGLE_SUCCESS
                }
        )

    @CommandNode("import", "<Plugin Name>", "<Default Server>")
    val import = ImportCommand.command

    @CommandNode("kick", "<player>", "[reason]")
    val kick = KickCommand.command

    @CommandNode("kickall", "[server]", "[reason]")
    val kickAll = KickAllCommand.command

    @CommandNode("send", "<player>", "<server>", "[reason]")
    val send = SendCommand.command

    @CommandNode("sendall", "<server>", "[reason]")
    val sendAll = SendAllCommand.command

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

    val command: LiteralCommandNode<CommandSource> = literal("avm")
        .executes { context -> context.buildHelp(this@AVMCommand.javaClass) }
        .then(reload)
        .then(info)
        .then(confirm)
        .then(import)
        .then(kickAll)
        .then(kick)
        .then(sendAll)
        .then(send)
        .build()
}
