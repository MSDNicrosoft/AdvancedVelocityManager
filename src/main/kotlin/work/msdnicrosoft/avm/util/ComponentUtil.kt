package work.msdnicrosoft.avm.util

import dev.vankka.enhancedlegacytext.EnhancedLegacyText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import taboolib.common.platform.function.warning
import work.msdnicrosoft.avm.config.AVMConfig.ChatBridge.Format

object ComponentUtil {

    val serializer = EnhancedLegacyText.get()

    fun createHoverEvent(format: Format, deserialize: String.() -> Component): HoverEvent<Component?>? =
        if (!format.hover.isNullOrEmpty()) {
            HoverEvent.showText(format.hover.joinToString("\n").deserialize())
        } else {
            null
        }

    fun createClickEvent(format: Format, replacer: String.() -> String): ClickEvent? {
        validateFormat(format).let { if (!it) return null }
        return when {
            !format.command.isNullOrEmpty() -> ClickEvent.runCommand(format.command.replacer())
            !format.suggest.isNullOrEmpty() -> ClickEvent.suggestCommand(format.suggest.replacer())
            !format.url.isNullOrEmpty() -> ClickEvent.openUrl(format.url.replacer())
            !format.clipboard.isNullOrEmpty() -> ClickEvent.copyToClipboard(format.clipboard.replacer())
            else -> null
        }
    }

    fun validateFormat(format: Format): Boolean {
        val conflicted = listOf(
            !format.command.isNullOrEmpty(),
            !format.suggest.isNullOrEmpty(),
            !format.url.isNullOrEmpty(),
            !format.clipboard.isNullOrEmpty(),
        ).count { it } > 1
        if (conflicted) {
            warning("Exactly one of 'command', 'suggest', 'url', or 'clipboard' should be provided and non-empty.")
        }
        return !conflicted
    }
}
