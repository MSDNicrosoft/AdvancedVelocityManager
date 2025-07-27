package work.msdnicrosoft.avm.util.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import work.msdnicrosoft.avm.util.component.ComponentUtil.miniMessage
import work.msdnicrosoft.avm.util.component.tr
import kotlin.math.ceil

/**
 * Pagination helper for commands that need to display a list of items spread across multiple pages.
 *
 * @param command The command name for the pagination component.
 */
class PageTurner(val command: String) {

    /**
     * Builds a pagination component with navigation buttons and a page indicator.
     *
     * @param currentPage The current page number.
     * @param maxPage The total number of pages.
     * @return A ComponentText representing the pagination component.
     */
    fun build(currentPage: Int, maxPage: Int): Component {
        return Component.join(
            JoinConfiguration.spaces(),
            if (currentPage == 1) {
                navigationButton("<gray><-")
            } else {
                navigationButton("<gold><-")
                    .hoverEvent(HoverEvent.showText(tr("avm.general.turn.to.previous")))
                    .clickEvent(ClickEvent.runCommand("$command ${currentPage - 1}"))
            },
            miniMessage.deserialize("<aqua>$currentPage/$maxPage"),
            if (currentPage == maxPage) {
                navigationButton("<gray>->")
            } else {
                navigationButton("<gold>->")
                    .hoverEvent(HoverEvent.showText(tr("avm.general.turn.to.next")))
                    .clickEvent(ClickEvent.runCommand("$command ${currentPage + 1}"))
            }
        )
    }

    companion object {
        /**
         * The number of items to display per page.
         */
        const val ITEMS_PER_PAGE = 10

        /**
         * Calculates the maximum number of pages for the given number of items.
         *
         * @param page The number of items.
         * @return The maximum number of pages.
         */
        fun getMaxPage(page: Int): Int = ceil(page.toFloat() / ITEMS_PER_PAGE.toFloat()).toInt()

        private fun navigationButton(arrow: String) = miniMessage.deserialize("<dark_gray>[$arrow<dark_gray>]")
    }
}
