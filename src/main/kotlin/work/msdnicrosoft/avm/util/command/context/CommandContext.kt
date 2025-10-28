package work.msdnicrosoft.avm.util.command.context

import com.highcapable.kavaref.extension.classOf
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.TranslatableBuilder
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.component.builder.text.ComponentBuilder
import work.msdnicrosoft.avm.util.component.builder.text.component
import kotlin.reflect.KProperty
import com.mojang.brigadier.context.CommandContext as BrigadierCommandContext

@Suppress("unused")
class CommandContext(val context: BrigadierCommandContext<CommandSource>) {
    inline operator fun <reified T : Any> getValue(thisRef: Any?, property: KProperty<*>): T {
        val parser: ArgumentParser<T> = ArgumentParser.of<T>()
            ?: return this.context.getArgument(property.name, classOf<T>())
        return parser.parse(this.context.getArgument(property.name, String::class.java))
    }

    fun sendMessage(message: ComponentLike) = this.context.source.sendMessage(message)

    inline fun sendMessage(
        joinConfiguration: JoinConfiguration = JoinConfiguration.spaces(),
        componentBuilder: ComponentBuilder.() -> Unit
    ) = this.sendMessage(component(joinConfiguration, componentBuilder))

    fun sendPlainMessage(message: String) = this.context.source.sendPlainMessage(message)

    fun sendRichMessage(message: String, vararg resolvers: TagResolver) =
        this.context.source.sendRichMessage(message, *resolvers)

    fun sendTranslatable(key: String, builder: TranslatableBuilder.() -> Unit = {}) = this.sendMessage(tr(key, builder))
}
