package work.msdnicrosoft.avm.util.component.builder.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextReplacementConfig
import work.msdnicrosoft.avm.annotations.dsl.ComponentDSL

@Suppress("unused")
@ComponentDSL
class TextReplacementsBuilder(private val parent: TextReplacementsBuilder? = null) {
    var override: Boolean = false

    private val replacements: MutableList<TextReplacementConfig> = mutableListOf()

    fun replacement(builder: TextReplacementConfig.Builder.() -> Unit) {
        this.replacements.add(TextReplacementConfig.builder().apply(builder).build())
    }

    internal fun replace(component: Component): Component {
        var replacer: TextReplacementsBuilder = this
        var currentComponent: Component = component
        do {
            currentComponent = replacer.replacements.fold(currentComponent) { curr, config ->
                curr.replaceText(config)
            }
            replacer = replacer.parent ?: TextReplacementsBuilder()
        } while (!replacer.override && replacer.parent != null)
        return currentComponent
    }
}

inline fun TextReplacementConfig.Builder.replace(crossinline builder: ComponentBuilder.(original: Component) -> Unit) {
    this.replacement { replaceBuilder ->
        component(JoinConfiguration.noSeparators()) { builder(replaceBuilder.build()) }
    }
}
