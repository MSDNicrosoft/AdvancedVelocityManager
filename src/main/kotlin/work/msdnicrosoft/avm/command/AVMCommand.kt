package work.msdnicrosoft.avm.command

import com.velocitypowered.api.util.ProxyVersion
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.annotations.CommandNode
import work.msdnicrosoft.avm.annotations.RootCommand
import work.msdnicrosoft.avm.command.utility.*
import work.msdnicrosoft.avm.module.command.session.CommandSessionManager
import work.msdnicrosoft.avm.module.command.session.ExecuteResult
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.buildHelp
import work.msdnicrosoft.avm.util.command.register
import work.msdnicrosoft.avm.util.command.unregister
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.server.task
import kotlin.time.Duration
import kotlin.time.measureTimedValue

@RootCommand("avm")
object AVMCommand {

    @CommandNode("reload")
    val reload = literalCommand("reload") {
        requires { hasPermission("avm.command.reload") }
        executes {
            val (success: Boolean, elapsed: Duration) = measureTimedValue { plugin.reload() }
            if (success) {
                sendTranslatable("avm.command.avm.reload.success") {
                    args { numeric("elapsed", elapsed.inWholeMilliseconds) }
                }
            } else {
                sendTranslatable("avm.command.avm.reload.failed")
            }
            Command.SINGLE_SUCCESS
        }
    }

    @CommandNode("info")
    val info = literalCommand("info") {
        requires { hasPermission("avm.command.info") }
        executes {
            val velocity: ProxyVersion = plugin.server.version
            sendTranslatable("avm.command.avm.info.plugin.name") {
                args { component("name", tr("avm.general.plugin.name")) }
            }
            sendTranslatable("avm.command.avm.info.plugin.version") {
                args { string("version", plugin.self.version.get()) }
            }
            sendTranslatable("avm.command.avm.info.server") {
                args { string("server", "${velocity.name} ${velocity.version}") }
            }
            Command.SINGLE_SUCCESS
        }
    }

    @CommandNode("confirm", "<session>")
    val confirm = literalCommand("confirm") {
        requires { hasPermission("avm.command.confirm") }
        greedyStringArgument("session") {
            executes {
                val session: String by this
                task {
                    val result: String = when (CommandSessionManager.executeAction(session)) {
                        ExecuteResult.SUCCESS -> return@task
                        ExecuteResult.EXPIRED -> "avm.command.avm.confirm.expired"
                        ExecuteResult.FAILED -> "avm.command.avm.confirm.failed"
                        ExecuteResult.NOT_FOUND -> "avm.command.avm.confirm.not_found"
                    }
                    sendTranslatable(result)
                }
                Command.SINGLE_SUCCESS
            }
        }
    }

    @CommandNode("import", "<pluginName>", "<defaultServer>")
    val import = ImportCommand.command

    @CommandNode("kick", "<player>", "[reason]")
    val kick = KickCommand.command

    @CommandNode("kickall", "[server]", "[reason]")
    val kickAll = KickAllCommand.command

    @CommandNode("send", "<player>", "<server>", "[reason]")
    val send = SendCommand.command

    @CommandNode("sendall", "<server>", "[reason]")
    val sendAll = SendAllCommand.command

    val command = literalCommand("avm") {
        executes { buildHelp(this@AVMCommand.javaClass) }
        then(reload)
        then(info)
        then(confirm)
        then(import)
        then(kickAll)
        then(kick)
        then(sendAll)
        then(send)
    }.build()

    fun init() {
        this.command.register()
    }

    fun disable() {
        this.command.unregister()
    }
}
