package work.msdnicrosoft.avm.util.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.component.CommandComponent
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.self
import work.msdnicrosoft.avm.annotations.ShouldShow
import kotlin.reflect.KClass

fun <T : Any> CommandComponent.buildHelper(commandRoot: KClass<T>, checkPermission: Boolean = true) {
    execute<ProxyCommandSender> { sender, _, _ ->
        val rootJavaClass = commandRoot.java
        val rootCommand = rootJavaClass.getAnnotation(CommandHeader::class.java)
        val rootName = rootCommand.name

        sender.sendLang(
            "general-help-header",
            self.version.get(),
            rootName
        )

        if (checkPermission && !sender.hasPermission(rootCommand.permission)) return@execute

        rootJavaClass.declaredFields.forEach { field ->
            field.let {
                val command = field.annotations.firstOrNull { it is CommandBody } as? CommandBody

                val shouldShow = field.isAnnotationPresent(ShouldShow::class.java)
                val noPermission = checkPermission && !sender.hasPermission(command?.permission ?: return@let)

                if (!shouldShow || noPermission || command?.hidden == true || command?.optional == true) {
                    return@let
                }

                sender.sendLang(
                    "general-help-each-command",
                    rootName,
                    field.name,
                    sender.asLangText("command-$rootName-${field.name}-description")
                )
            }
        }
    }
}
