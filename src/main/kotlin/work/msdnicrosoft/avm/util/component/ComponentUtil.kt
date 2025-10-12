package work.msdnicrosoft.avm.util.component

import net.kyori.adventure.text.event.ClickEvent

object ComponentUtil {
    /**
     * Creates a ClickEvent based on the provided [format] and function [replace].
     */
    fun createClickEvent(format: Format, replace: String.() -> String): ClickEvent? =
        when {
            !format.command.isNullOrEmpty() -> ClickEvent.runCommand(format.command.replace())
            !format.suggest.isNullOrEmpty() -> ClickEvent.suggestCommand(format.suggest.replace())
            !format.url.isNullOrEmpty() -> ClickEvent.openUrl(format.url.replace())
            !format.clipboard.isNullOrEmpty() -> ClickEvent.copyToClipboard(format.clipboard.replace())
            else -> null
        }
}
