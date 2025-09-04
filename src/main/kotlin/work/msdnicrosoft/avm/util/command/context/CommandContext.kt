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
    inline operator fun <reified T : Any> getValue(thisRef: Any?, property: KProperty<*>): T =
        when (classOf<T>()) {
            BrigadierCommandContext::class.java -> this.context as T
            S::class.java -> this.context.source as T
            else -> this.context.getArgument<T>(property.name, T::class.java)
        }

    fun sendMessage(message: Component) = this.context.source.sendMessage(message)

    fun sendPlainMessage(message: String) = this.context.source.sendPlainMessage(message)

    fun sendRichMessage(message: String) = this.context.source.sendRichMessage(message)

    fun sendRichMessage(message: String, vararg resolvers: TagResolver) =
        this.context.source.sendRichMessage(message, *resolvers)

    fun sendTranslatable(key: String, vararg args: ComponentLike) = this.context.source.sendTranslatable(key, *args)
}
