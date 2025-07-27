package work.msdnicrosoft.avm.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.command.utility.*
import work.msdnicrosoft.avm.module.CommandSessionManager
import work.msdnicrosoft.avm.module.CommandSessionManager.ExecuteResult
import work.msdnicrosoft.avm.util.command.*
import work.msdnicrosoft.avm.util.server.task
import kotlin.time.measureTime

object AVMCommand {

    fun init() {
        command.register()
    }

    fun disable() {
        command.unregister()
    }

    val reload: LiteralArgumentBuilder<CommandSource> = literal("reload")
        .requires { source -> source.hasPermission("avm.command.reload") }
        .executes { context ->
            var success = false
            val elapsed = measureTime { success = plugin.reload() }
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

    val info: LiteralArgumentBuilder<CommandSource> = literal("info")
        .requires { source -> source.hasPermission("avm.command.info") }
        .executes { context ->
            val velocity = plugin.server.version
            // TODO Enabled & Disabled modules
            context.source.sendTranslatable(
                "avm.command.avm.info.plugin.name",
                Argument.string("name", plugin.self.name.get())
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

    val confirm: LiteralArgumentBuilder<CommandSource> = literal("confirm")
        .requires { source -> source.hasPermission("avm.command.confirm") }
        .then(
            wordArgument("session")
                .executes { context ->
                    task {
                        val result = when (CommandSessionManager.executeAction(context.getString("session"))) {
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
//        .executes { context -> context.buildHelper(this@AVMCommand.javaClass) }
        .then(reload)
        .then(info)
        .then(confirm)
        .then(ImportCommand.command)
        .then(KickAllCommand.command)
        .then(KickCommand.command)
        .then(SendAllCommand.command)
        .then(SendCommand.command)
        .build()
}
