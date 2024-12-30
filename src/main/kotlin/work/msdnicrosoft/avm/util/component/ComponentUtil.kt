package work.msdnicrosoft.avm.util.component

import dev.vankka.enhancedlegacytext.EnhancedLegacyText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.logger

object ComponentUtil {

    val serializer: EnhancedLegacyText = EnhancedLegacyText.get()

    /**
     * Creates a HoverEvent based on the provided format and deserialization function.
     *
     * @param format The format to extract hover text from.
     * @param deserialize A function to deserialize the hover text into a Component.
     *
     * @return A HoverEvent showing the deserialized hover text, or null if the format has no hover text.
     */
    fun createHoverEvent(format: Format, deserialize: String.() -> Component): HoverEvent<Component?>? =
        if (!format.hover.isNullOrEmpty()) {
            HoverEvent.showText(format.hover.joinToString("\n").deserialize())
        } else {
            null
        }

    /**
     * Creates a ClickEvent based on the provided format and replacer function.
     *
     * @param format The format to extract click event data from.
     * @param replacer A function to replace placeholders in the click event data.
     *
     * @return A ClickEvent representing the action to be performed when clicked, or null if the format is invalid.
     */
    fun createClickEvent(format: Format, replacer: String.() -> String): ClickEvent? {
        if (!validateFormat(format)) return null
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
            logger.warn("Format $format is incorrect!")
            logger.warn("Exactly one of 'command', 'suggest', 'url', or 'clipboard' should be provided and non-empty.")
        }
        return !conflicted
    }
}
