package work.msdnicrosoft.avm.util.component.builder.minimessage.tag

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.translation.Argument
import work.msdnicrosoft.avm.annotations.dsl.ComponentDSL

@ComponentDSL
class TranslatableBuilder(private val key: String) {
    private val arguments: MutableList<ComponentLike> = mutableListOf()

    fun args(builder: Arguments.() -> Unit) {
        this.arguments.addAll(Arguments().apply(builder).build())
    }

    fun build(): Component = Component.translatable(this.key, this.arguments)

    @Suppress("unused")
    class Arguments {
        private val args: MutableList<ComponentLike> = mutableListOf()

        fun bool(name: String, value: Boolean) {
            this.args.add(Argument.bool(name, value))
        }

        fun numeric(name: String, value: Number) {
            this.args.add(Argument.numeric(name, value))
        }

        fun string(name: String, value: String) {
            this.args.add(Argument.string(name, value))
        }

        fun component(name: String, value: ComponentLike) {
            this.args.add(Argument.component(name, value))
        }

        internal fun build(): List<ComponentLike> = this.args
    }
}

inline fun tr(key: String, builder: TranslatableBuilder.() -> Unit = {}): Component =
    TranslatableBuilder(key).apply(builder).build()
