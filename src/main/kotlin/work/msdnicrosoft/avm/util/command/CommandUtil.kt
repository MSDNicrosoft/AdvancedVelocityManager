package work.msdnicrosoft.avm.util.command

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.service.PlatformCommand
import taboolib.common.util.subList
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.chat.colored
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.self
import work.msdnicrosoft.avm.annotations.ShouldShow
import kotlin.reflect.KClass

object CommandUtil {

    private val shouldShowAnnotation = ShouldShow::class.java

    private val commandHeaderAnnotation = CommandHeader::class.java

    /**
     * Portions of this code are from TrMenu
     *
     * https://github.com/TrPlugins/TrMenu/blob/076bacb874cc1a2217ba8ccd4909405b28e7170d
     * /plugin/src/main/kotlin/trplugins/menu/module/internal/command/CommandHandler.kt
     */
    @Suppress("LoopWithTooManyJumpStatements", "CyclomaticComplexMethod")
    fun <T : Any> CommandComponent.buildHelper(commandRoot: KClass<T>, checkPermission: Boolean = true) {
        execute<ProxyCommandSender> { sender, _, _ ->
            val rootJavaClass = commandRoot.java
            val rootCommand = rootJavaClass.getAnnotation(commandHeaderAnnotation)
            val rootName = rootCommand.name

            if (checkPermission && !sender.hasPermission(rootCommand.permission)) {
                return@execute
            }

            sender.sendLang("general-help-header", self.version.get(), rootName)

            for (field in rootJavaClass.declaredFields) {
                field.trySetAccessible()

                val command = field.annotations.firstOrNull { it is CommandBody } as? CommandBody

                val shouldShow = field.isAnnotationPresent(shouldShowAnnotation)
                val noPermission = checkPermission && !sender.hasPermission(command?.permission ?: continue)

                if (!shouldShow || noPermission || command?.hidden == true || command?.optional == true) continue

                val arguments = field.getAnnotation(shouldShowAnnotation).arguments.joinToString(" ") {
                    when {
                        "{" in it -> "&c$it"
                        "[" in it -> "&8$it"
                        "<" in it -> "&7$it"
                        else -> it
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
     * Portions of this code are from TabooLib
     * https://github.com/TabooLib/taboolib/blob/8a998b946c4d4a3a93168cb84a40e31391967713
     * /common-platform-api/src/main/kotlin/taboolib/common/platform/command/component/CommandBase.kt
     */
    fun incorrectCommandFeedback(
        sender: ProxyCommandSender,
        context: CommandContext<ProxyCommandSender>,
        index: Int,
        state: Int
    ) {
        val args = subList(context.getProperty<Array<String>>("realArgs")!!.toList(), 0, index)
        val str = buildString {
            append(context.name)
            if (args.size > 1) append(" ${subList(args, 0, args.size - 1).joinToString(" ").trim()}")
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
