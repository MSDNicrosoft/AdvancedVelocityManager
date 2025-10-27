package work.msdnicrosoft.avm.util.component.builder.style

import net.kyori.adventure.text.event.ClickEvent
import work.msdnicrosoft.avm.annotations.dsl.ComponentDSL
import java.net.URL

@Suppress("unused")
@ComponentDSL
class ClickEventBuilder {
    private var clickEvent: ClickEvent? = null

    fun fromEvent(event: ClickEvent?) {
        this.clickEvent = event
    }

    fun openUrl(url: String?) {
        if (url == null) return
        this.clickEvent = ClickEvent.openUrl(url)
    }

    fun openUrl(url: URL?) {
        if (url == null) return
        this.clickEvent = ClickEvent.openUrl(url)
    }

    fun runCommand(command: String?) {
        if (command == null) return
        this.clickEvent = ClickEvent.runCommand(command)
    }

    fun suggestCommand(command: String?) {
        if (command == null) return
        this.clickEvent = ClickEvent.suggestCommand(command)
    }

    fun copyToClipboard(text: String?) {
        if (text == null) return
        this.clickEvent = ClickEvent.copyToClipboard(text)
    }

    fun build(): ClickEvent? = this.clickEvent
}

inline fun clickEvent(builder: ClickEventBuilder.() -> Unit): ClickEvent? = ClickEventBuilder().apply(builder).build()
