package work.msdnicrosoft.avm.util.component

import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags

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
            !format.validate() -> null
            !format.command.isNullOrEmpty() -> ClickEvent.runCommand(format.command.replace())
            !format.suggest.isNullOrEmpty() -> ClickEvent.suggestCommand(format.suggest.replace())
            !format.url.isNullOrEmpty() -> ClickEvent.openUrl(format.url.replace())
            !format.clipboard.isNullOrEmpty() -> ClickEvent.copyToClipboard(format.clipboard.replace())
            else -> null
        }
}
