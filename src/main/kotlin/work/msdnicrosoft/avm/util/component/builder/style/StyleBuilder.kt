package work.msdnicrosoft.avm.util.component.builder.style

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import work.msdnicrosoft.avm.annotations.dsl.ComponentDSL
import work.msdnicrosoft.avm.util.component.Format

@Suppress("unused")
@ComponentDSL
class StyleBuilder {
    private var style: Style = Style.empty()

    fun color(color: TextColor) {
        this.style = this.style.color(color)
    }

    fun shadowColor(shadowColor: ShadowColor) {
        this.style = this.style.shadowColor(shadowColor)
    }

    fun italic(italic: Boolean = true) {
        this.style = this.style.decoration(TextDecoration.ITALIC, italic)
    }

    fun bold(bold: Boolean = true) {
        this.style = this.style.decoration(TextDecoration.BOLD, bold)
    }

    fun strikethrough(strikethrough: Boolean = true) {
        this.style = this.style.decoration(TextDecoration.STRIKETHROUGH, strikethrough)
    }

    fun obfuscated(obfuscated: Boolean = true) {
        this.style = this.style.decoration(TextDecoration.OBFUSCATED, obfuscated)
    }

    fun font(key: Key) {
        this.style = this.style.font(key)
    }

    fun hoverText(text: String?) {
        if (text == null) return
        this.style = this.style.hoverEvent(HoverEvent.showText(Component.text(text)))
    }

    fun hoverText(text: ComponentLike?) {
        if (text == null) return
        this.style = this.style.hoverEvent(HoverEvent.showText(text))
    }

    fun insertion(text: String?) {
        if (text == null) return
        this.style = this.style.insertion(text)
    }

    fun click(builder: ClickEventBuilder.() -> Unit) {
        this.style = this.style.clickEvent(clickEvent(builder))
    }

    fun click(format: Format) {
        this.click {
            runCommand(format.command)
            suggestCommand(format.suggest)
            openUrl(format.url)
            copyToClipboard(format.clipboard)
        }
    }

    fun fromClickEvent(event: ClickEvent?) {
        this.style = this.style.clickEvent(event)
    }

    fun build(): Style = this.style
}

inline fun style(builder: StyleBuilder.() -> Unit): Style = StyleBuilder().apply(builder).build()

inline infix fun Component.styled(builder: StyleBuilder.() -> Unit): Component = this.style(style(builder))
