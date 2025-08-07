package work.msdnicrosoft.avm.util.command.context

import com.highcapable.kavaref.extension.classOf
import com.velocitypowered.api.command.CommandSource
import kotlin.reflect.KProperty
import com.mojang.brigadier.context.CommandContext as BrigadierCommandContext

typealias S = CommandSource

class CommandContext<S>(val context: BrigadierCommandContext<S>) {
    inline operator fun <reified T : Any> getValue(thisRef: Any?, property: KProperty<*>): T =
        when (classOf<T>()) {
            BrigadierCommandContext::class.java -> context as T
            S::class.java -> context.source as T
            else -> context.getArgument<T>(property.name, T::class.java)
        }
}
