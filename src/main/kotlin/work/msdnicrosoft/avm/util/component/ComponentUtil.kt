package work.msdnicrosoft.avm.util.component

import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger

object ComponentUtil {

    val miniMessage = MiniMessage.miniMessage()

    val styleOnlyMiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.font())
                .resolver(StandardTags.color())
                .resolver(StandardTags.decorations())
                .resolver(StandardTags.gradient())
                .resolver(StandardTags.rainbow())
                .resolver(StandardTags.reset())
                .resolver(StandardTags.shadowColor())
                .build()
        ).build()

    /**
     * Creates a ClickEvent based on the provided format and replacer function.
     *
     * @param format The format to extract click event data from.
     * @param replace A function to replace placeholders in the click event data.
     *
     * @return A ClickEvent representing the action to be performed when clicked, or null if the format is invalid.
     */
    fun createClickEvent(format: Format, replace: String.() -> String): ClickEvent? =
        when {
            !validateFormat(format) -> null
            !format.command.isNullOrEmpty() -> ClickEvent.runCommand(format.command.replace())
            !format.suggest.isNullOrEmpty() -> ClickEvent.suggestCommand(format.suggest.replace())
            !format.url.isNullOrEmpty() -> ClickEvent.openUrl(format.url.replace())
            !format.clipboard.isNullOrEmpty() -> ClickEvent.copyToClipboard(format.clipboard.replace())
            else -> null
        }

    /**
     * Validates a given format.
     *
     * @param format The format to be validated.
     *
     * @return True if the format is valid, false otherwise.
     */
    private fun validateFormat(format: Format): Boolean {
        if (format.text.isEmpty()) {
            logger.warn("Invalid format: {}", format)
            logger.warn("Text cannot be empty or blank.")
            return false
        }

        val conflicted = listOf(
            !format.command.isNullOrEmpty(),
            !format.suggest.isNullOrEmpty(),
            !format.url.isNullOrEmpty(),
            !format.clipboard.isNullOrEmpty(),
        ).count { it } > 1
        if (conflicted) {
            logger.warn("Invalid format: {}", format)
            logger.warn("Exactly one of 'command', 'suggest', 'url', or 'clipboard' should be provided and non-empty.")
            return false
        }
        return true
    }
}
