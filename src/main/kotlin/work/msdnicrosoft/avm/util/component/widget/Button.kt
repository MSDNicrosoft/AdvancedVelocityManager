package work.msdnicrosoft.avm.util.component.widget

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import work.msdnicrosoft.avm.util.component.builder.style.ClickEventBuilder
import work.msdnicrosoft.avm.util.component.builder.style.clickEvent
import work.msdnicrosoft.avm.util.component.builder.style.styled
import work.msdnicrosoft.avm.util.component.builder.text.component

class Button private constructor(
    private val text: String,
    private val borderType: BorderType,
    private val color: Builder.Color,
    private val enableWhen: () -> Boolean,
    private val hover: Builder.Hover,
    private val click: Builder.Click,
) {
    fun toComponent(): Component {
        val isEnabled: Boolean = enableWhen()
        return component {
            text(borderType.left) styled { color(color.border) }
            text(text) styled { color(if (isEnabled) color.enabled else color.disabled) }
            text(borderType.right) styled { color(color.border) }
        } styled {
            click { fromEvent(if (isEnabled) click.whenEnabled else click.whenDisabled) }
            hoverText(if (isEnabled) hover.whenEnabled else hover.whenDisabled)
        }
    }

    @Suppress("unused")
    enum class BorderType(val left: String, val right: String) {
        NONE("", ""),
        ROUND("(", ")"),
        SQUARE("[", "]"),
        CURLY("{", "}"),
        ANGLE("<", ">")
    }

    @Suppress("unused")
    class Builder(private val text: String) {
        private var borderType: BorderType = BorderType.SQUARE
        private var enableWhen: () -> Boolean = { true }

        private val color: Color = Color()
        private val hover: Hover = Hover()
        private val click: Click = Click()

        fun borderType(borderType: BorderType) {
            this.borderType = borderType
        }

        fun color(color: Color.() -> Unit) {
            this.color.apply(color)
        }

        fun enableWhen(enableWhen: () -> Boolean) {
            this.enableWhen = enableWhen
        }

        fun click(click: Click.() -> Unit) {
            this.click.apply(click)
        }

        fun hover(hover: Hover.() -> Unit) {
            this.hover.apply(hover)
        }

        fun build(): Button = Button(
            text = this.text,
            borderType = this.borderType,
            color = this.color,
            enableWhen = this.enableWhen,
            hover = this.hover,
            click = this.click,
        )

        class Color internal constructor() {
            var border: TextColor = NamedTextColor.DARK_GRAY
                private set

            var enabled: TextColor = NamedTextColor.GOLD
                private set

            var disabled: TextColor = NamedTextColor.GRAY
                private set

            fun border(color: TextColor) {
                this.border = color
            }

            fun enabled(color: TextColor) {
                this.enabled = color
            }

            fun disabled(color: TextColor) {
                this.disabled = color
            }
        }

        class Click internal constructor() {
            var whenDisabled: ClickEvent? = null
                private set

            var whenEnabled: ClickEvent? = null
                private set

            fun whenDisabled(builder: ClickEventBuilder.() -> Unit) {
                this.whenDisabled = clickEvent(builder)
            }

            fun whenEnabled(builder: ClickEventBuilder.() -> Unit) {
                this.whenEnabled = clickEvent(builder)
            }
        }

        class Hover internal constructor() {
            var whenEnabled: ComponentLike = Component.empty()
                private set

            var whenDisabled: ComponentLike = Component.empty()
                private set

            fun whenEnabled(component: ComponentLike) {
                this.whenEnabled = component
            }

            fun whenDisabled(component: ComponentLike) {
                this.whenDisabled = component
            }
        }
    }
}

inline fun button(text: String, builder: Button.Builder.() -> Unit): Component =
    Button.Builder(text).apply(builder).build().toComponent()
