package work.msdnicrosoft.avm.util.component.builder.minimessage.tag

import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import work.msdnicrosoft.avm.annotations.dsl.ComponentDSL

@Suppress("unused")
@ComponentDSL
class PlaceholdersBuilder {
    private val placeholders: MutableList<TagResolver> = mutableListOf()

    fun parsed(key: String, value: String) {
        this.placeholders.add(Placeholder.parsed(key, value))
    }

    fun unparsed(key: String, value: String) {
        this.placeholders.add(Placeholder.unparsed(key, value))
    }

    fun numeric(key: String, value: Number) {
        this.placeholders.add(Placeholder.parsed(key, value.toString()))
    }

    fun component(key: String, value: ComponentLike) {
        this.placeholders.add(Placeholder.component(key, value))
    }

    fun styling(key: String, vararg style: StyleBuilderApplicable) {
        this.placeholders.add(Placeholder.styling(key, *style))
    }

    fun tagResolver(key: String, handler: (arguments: ArgumentQueue, context: Context) -> Tag) {
        this.placeholders.add(TagResolver.resolver(key, handler))
    }

    fun tagResolvers(vararg resolvers: TagResolver) {
        this.placeholders.addAll(resolvers)
    }

    fun tagResolvers(resolvers: Iterable<TagResolver>) {
        this.placeholders.addAll(resolvers)
    }

    fun build(): List<TagResolver> = this.placeholders
}

inline fun placeholders(builder: PlaceholdersBuilder.() -> Unit): List<TagResolver> =
    PlaceholdersBuilder().apply(builder).build()
