package work.msdnicrosoft.avm.util.command.brigadier

import kotlin.reflect.KProperty
import com.mojang.brigadier.context.CommandContext as BrigadierCommandContext

class CommandContext<S>(val context: BrigadierCommandContext<S>) {
    inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>): T =
        when (T::class.java) {
            BrigadierCommandContext::class.java -> context as T
            S::class.java -> context.source as T
            else -> context.getArgument<T>(property.name, T::class.java)
        }
}
