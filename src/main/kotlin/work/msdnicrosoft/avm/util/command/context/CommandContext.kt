package work.msdnicrosoft.avm.util.command.context

import com.highcapable.kavaref.extension.classOf
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import work.msdnicrosoft.avm.util.component.sendTranslatable
import kotlin.reflect.KProperty
import com.mojang.brigadier.context.CommandContext as BrigadierCommandContext

typealias S = CommandSource

@Suppress("unused")
class CommandContext(val context: BrigadierCommandContext<S>) {
    inline operator fun <reified T : Any> getValue(thisRef: Any?, property: KProperty<*>): T = when (classOf<T>()) {
        BrigadierCommandContext::class.java -> context as T
        S::class.java -> context.source as T
        else -> context.getArgument<T>(property.name, T::class.java)
    }

    fun sendMessage(message: Component) = context.source.sendMessage(message)

    fun sendPlainMessage(message: String) = context.source.sendPlainMessage(message)

    fun sendRichMessage(message: String) = context.source.sendRichMessage(message)

    fun sendRichMessage(message: String, vararg resolvers: TagResolver) =
        context.source.sendRichMessage(message, *resolvers)

    fun sendTranslatable(key: String, vararg args: ComponentLike) = context.source.sendTranslatable(key, *args)
}
