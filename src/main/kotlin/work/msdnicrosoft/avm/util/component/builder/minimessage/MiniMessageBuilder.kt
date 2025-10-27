package work.msdnicrosoft.avm.util.component.builder.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import work.msdnicrosoft.avm.annotations.dsl.ComponentDSL
import work.msdnicrosoft.avm.util.component.ComponentSerializer
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.PlaceholdersBuilder
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.placeholders as placeholdersBuilder

@ComponentDSL
class MiniMessageBuilder(val text: String, private val provider: MiniMessage) {
    private val placeholders: MutableList<TagResolver> = mutableListOf()

    fun placeholders(builder: PlaceholdersBuilder.() -> Unit) {
        this.placeholders.addAll(placeholdersBuilder(builder))
    }

    fun build(): Component = this.provider.deserialize(this.text, TagResolver.resolver(this.placeholders))
}

inline fun miniMessage(
    text: String,
    provider: ComponentSerializer = ComponentSerializer.MINI_MESSAGE,
    builder: MiniMessageBuilder.() -> Unit = {}
): Component = MiniMessageBuilder(text, provider.serializer as MiniMessage).apply(builder).build()
