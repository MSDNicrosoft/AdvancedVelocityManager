package work.msdnicrosoft.avm.util.command

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.service.PlatformCommand
import taboolib.common.reflect.getAnnotationIfPresent
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.chat.colored
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.self
import work.msdnicrosoft.avm.annotations.ShouldShow
import kotlin.reflect.KClass

object CommandUtil {

    private val SHOULD_SHOW_ANNOTATION = ShouldShow::class.java
    private val COMMAND_HEADER_ANNOTATION = CommandHeader::class.java
    private val COMMAND_BODY_ANNOTATION = CommandBody::class.java

    /**
     * Portions of this code are modified from TrMenu
     *
     * https://github.com/TrPlugins/TrMenu/blob/076bacb874cc1a2217ba8ccd4909405b28e7170d
     * /plugin/src/main/kotlin/trplugins/menu/module/internal/command/CommandHandler.kt
     */
    fun <T : Any> CommandComponent.buildHelper(commandRoot: KClass<T>, checkPermission: Boolean = true) {
        execute<ProxyCommandSender> { sender, _, _ ->
            val rootJavaClass = commandRoot.java
            val rootCommand = rootJavaClass.getAnnotationIfPresent(COMMAND_HEADER_ANNOTATION) ?: return@execute
            val rootName = rootCommand.name

            sender.sendLang("general-help-header", self.version.get(), rootName)

            rootJavaClass.declaredFields.asSequence()
                .onEach { it.trySetAccessible() }
                .filter { field ->
                    field.getAnnotationIfPresent(COMMAND_BODY_ANNOTATION)?.let { command ->
                        val hasPermission = !checkPermission || sender.hasPermission(command.permission)
                        !command.hidden && !command.optional && hasPermission
                    } == true
                }.forEach { field ->
                    val rawArguments = field.getAnnotationIfPresent(SHOULD_SHOW_ANNOTATION)?.arguments ?: return@forEach
                    val arguments = rawArguments.joinToString(" ") { arg ->
                        when (arg.firstOrNull()) {
                            '{' -> "&c$arg"
                            '[' -> "&8$arg"
                            '<' -> "&7$arg"
                            else -> arg
                        }
                    }
                    sender.sendLang(
                        "general-help-each-command",
                        rootName,
                        field.name,
                        arguments,
                        sender.asLangText("command-$rootName-${field.name}-description")
                    )
                }
        }
    }

    /**
     * Portions of this code are modified from TabooLib
     * https://github.com/TabooLib/taboolib/blob/8a998b946c4d4a3a93168cb84a40e31391967713
     * /common-platform-api/src/main/kotlin/taboolib/common/platform/command/component/CommandBase.kt
     */
    @Suppress("MagicNumber")
    fun incorrectCommandFeedback(
        sender: ProxyCommandSender,
        context: CommandContext<ProxyCommandSender>,
        index: Int,
        state: Int
    ) {
        val args = context.getProperty<Array<String>>("realArgs")?.toList()?.subList(0, index).orEmpty()
        val str = buildString {
            append(context.name)
            if (args.size > 1) append(" ${args.subList(0, args.size - 1).joinToString(" ").trim()}")
            if (length > 10) append("...${substring(length - 10, length)}")
            if (args.isNotEmpty()) append(" &c&n${args.last()}")
        }

        val command = PlatformFactory.getService<PlatformCommand>()
        if (command.isSupportedUnknownCommand()) {
            command.unknownCommand(sender, str, state)
        } else {
            when (state) {
                1 -> sender.sendLang("unknown-command")
                2 -> sender.sendLang("unknown-argument")
            }
            sender.sendMessage("&7$str&r&c&o<--[${sender.asLangText("unknown-here")}]".colored())
        }
    }

    fun incorrectSenderFeedback(
        sender: ProxyCommandSender,
        @Suppress("unused")
        context: CommandContext<ProxyCommandSender>
    ) = sender.sendLang("unknown-sender")
}
