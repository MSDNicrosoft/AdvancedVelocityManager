package work.msdnicrosoft.avm.util.component.builder.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.Style
import work.msdnicrosoft.avm.annotations.dsl.ComponentDSL
import work.msdnicrosoft.avm.util.component.ComponentSerializer
import work.msdnicrosoft.avm.util.component.builder.minimessage.MiniMessageBuilder
import work.msdnicrosoft.avm.util.component.builder.minimessage.miniMessage
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.TranslatableBuilder
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.component.builder.style.StyleBuilder
import work.msdnicrosoft.avm.util.component.builder.style.style

@Suppress("unused")
@ComponentDSL
class ComponentBuilder(private val joinConfiguration: JoinConfiguration) {
    private val components: MutableList<Component> = mutableListOf()

    private var replacements: TextReplacementsBuilder? = null

    fun empty(): ComponentBuilder = this.apply { this.components.add(Component.empty()) }

    fun newline(): ComponentBuilder = this.apply { this.components.add(Component.newline()) }

    fun space(): ComponentBuilder = this.apply { this.components.add(Component.space()) }

    fun keybind(keybind: String): ComponentBuilder = this.apply { this.components.add(Component.keybind(keybind)) }

    fun text(text: String): ComponentBuilder = this.apply { this.components.add(Component.text(text)) }

    fun text(text: () -> String): ComponentBuilder = this.apply { this.components.add(Component.text(text())) }

    fun translatable(key: String, builder: TranslatableBuilder.() -> Unit = {}): ComponentBuilder = this.apply {
        this.components.add(tr(key, builder))
    }

    fun componentLike(component: ComponentLike): ComponentBuilder = this.apply {
        this.components.add(component.asComponent())
    }

    fun mini(
        text: String,
        provider: ComponentSerializer = ComponentSerializer.MINI_MESSAGE,
        builder: MiniMessageBuilder.() -> Unit = {}
    ): ComponentBuilder = this.apply { this.components.add(miniMessage(text, provider, builder)) }

    fun replacements(builder: TextReplacementsBuilder.() -> Unit): ComponentBuilder = this.apply {
        val parent: TextReplacementsBuilder? = this.replacements
        this.replacements = TextReplacementsBuilder(parent).apply(builder)
    }

    inline infix fun styled(builder: StyleBuilder.() -> Unit): ComponentBuilder = this.with(style(builder))

    infix fun with(style: Style): ComponentBuilder = this.apply {
        val last: Component = this.components.last()
        this.components[this.components.lastIndex] = last.style(style)
    }

    fun build(): Component = Component.join(
        this.joinConfiguration,
        this.components.map { this.replacements?.replace(it) ?: it }
    )
}

inline fun component(
    joinConfiguration: JoinConfiguration = JoinConfiguration.noSeparators(),
    builder: ComponentBuilder.() -> Unit
): Component = ComponentBuilder(joinConfiguration).apply(builder).build()
