package work.msdnicrosoft.avm.util.component

import net.kyori.adventure.text.event.ClickEvent

object ComponentUtil {
    /**
     * Creates a ClickEvent based on the provided format and replacer function.
     *
     * @param format The format to extract click event data from.
     * @param replace A function to replace placeholders in the click event data.
     *
     * @return A ClickEvent representing the action to be performed when clicked, or null if the format is invalid.
     */
    fun createClickEvent(format: Format, replace: String.() -> String): ClickEvent? = when {
        !format.validate() -> null
        !format.command.isNullOrEmpty() -> ClickEvent.runCommand(format.command.replace())
        !format.suggest.isNullOrEmpty() -> ClickEvent.suggestCommand(format.suggest.replace())
        !format.url.isNullOrEmpty() -> ClickEvent.openUrl(format.url.replace())
        !format.clipboard.isNullOrEmpty() -> ClickEvent.copyToClipboard(format.clipboard.replace())
        else -> null
    }
}
