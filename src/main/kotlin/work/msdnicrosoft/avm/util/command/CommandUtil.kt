package work.msdnicrosoft.avm.util.command

object CommandUtil {

    /**
     * Portions of this code are modified from TrMenu
     *
     * https://github.com/TrPlugins/TrMenu/blob/076bacb874cc1a2217ba8ccd4909405b28e7170d
     * /plugin/src/main/kotlin/trplugins/menu/module/internal/command/CommandHandler.kt
     */
//    fun <T : Any> CommandContext<CommandSource>.buildHelper(
//        commandRoot: Class<T>,
//        checkPermission: Boolean = true
//    ): Int {
//        val rootJavaClass = commandRoot
//
//        this.source.sendTranslatable(
//            "avm.general.help.header.1.text",
//            Argument.component(
//                "name",
//                tr("avm.general.help.header.1.name.text")
//                    .hoverEvent(HoverEvent.showText(tr("avm.general.help.header.1.name.hover")))
//            ),
//            Argument.string("version", self.version.get()),
//        )
//        this.source.sendTranslatable(
//            "avm.general.help.header.2.text",
//            Argument.string("root_command", rootName)
//        )
//        this.source.sendTranslatable("avm.general.help.header.subcommands")
//
//        rootJavaClass.declaredFields.asSequence()
//            .onEach { it.trySetAccessible() }
//            .filter { field ->
//                field.getAnnotationIfPresent(COMMAND_BODY_ANNOTATION)?.let { command ->
//                    val hasPermission = !checkPermission || this.source.hasPermission(command.permission)
//                    !command.hidden && !command.optional && hasPermission
//                } == true
//            }.forEach { field ->
//                val rawArguments = field.getAnnotationIfPresent(SHOULD_SHOW_ANNOTATION)?.arguments ?: return@forEach
//                val arguments = rawArguments.joinToString(" ") { arg ->
//                    when (arg.firstOrNull()) {
//                        '{' -> "&c$arg"
//                        '[' -> "&8$arg"
//                        '<' -> "&7$arg"
//                        else -> arg
//                    }
//                }
//                this.source.sendMessage(
//                    miniMessage.deserialize(
//                        "    <dark_gray>- <white><subcommand> <arguments>",
//                        Placeholder.unparsed("subcommand", field.name),
//                        Placeholder.unparsed("arguments", arguments),
//                    ).hoverEvent(HoverEvent.showText(tr("avm.command.$rootName.${field.name}.description")))
//                )
//            }
//
//        return Command.SINGLE_SUCCESS
//    }
}
