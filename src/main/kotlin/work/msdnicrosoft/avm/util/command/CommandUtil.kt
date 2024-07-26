/**
 * Portions of this code are from TabooLib
 * https://github.com/TabooLib/taboolib/blob/8a998b946c4d4a3a93168cb84a40e31391967713
 * /common-platform-api/src/main/kotlin/taboolib/common/platform/command/component/CommandBase.kt
 */

package work.msdnicrosoft.avm.util.command

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.service.PlatformCommand
import taboolib.common.util.subList
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.last
import kotlin.collections.toList
import kotlin.text.substring
import kotlin.text.trim

object CommandUtil {

    fun incorrectCommandFeedback(
        sender: ProxyCommandSender,
        context: CommandContext<ProxyCommandSender>,
        index: Int,
        state: Int
    ) {
        val args = subList(context.getProperty<Array<String>>("realArgs")!!.toList(), 0, index)
        var str = context.name
        if (args.size > 1) {
            str += " ${subList(args, 0, args.size - 1).joinToString(" ").trim()}"
        }
        if (str.length > 10) {
            str = "...${str.substring(str.length - 10, str.length)}"
        }
        if (args.isNotEmpty()) {
            str += " &c&n${args.last()}"
        }
        val command = PlatformFactory.getService<PlatformCommand>()
        if (command.isSupportedUnknownCommand()) {
            command.unknownCommand(sender, str, state)
        } else {
            when (state) {
                1 -> sender.sendLang("unknown-command")
                2 -> sender.sendLang("unknown-argument")
            }
            sender.sendMessage("&7$str&r&c&o<--[${sender.asLangText("unknown-here")}]")
        }
    }

    fun incorrectSenderFeedback(
        sender: ProxyCommandSender,
        @Suppress("unused")
        context: CommandContext<ProxyCommandSender>
    ) = sender.sendLang("unknown-sender")
}
