package work.msdnicrosoft.avm.util.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.lang.asLangText
import kotlin.math.ceil

/**
 * Pagination helper for commands that need to display a list of items spread across multiple pages.
 *
 * @param sender The command sender.
 * @param command The command name for the pagination component.
 */
class PageTurner(val sender: ProxyCommandSender, val command: String) {

    /**
     * Builds a pagination component with navigation buttons and a page indicator.
     *
     * @param currentPage The current page number.
     * @param maxPage The total number of pages.
     * @return A ComponentText representing the pagination component.
     */
    fun build(currentPage: Int, maxPage: Int): ComponentText {
        val previous = if (currentPage == 1) {
            Components.text("§8[§7<-§8]")
        } else {
            Components.text("§8[§6<-§8]")
                .hoverText(sender.asLangText("general-turn-to-previous-page"))
                .clickRunCommand("$command ${currentPage - 1}")
        }
        val pageOf = Components.text("§b$currentPage§b/§b$maxPage")
        val next = if (currentPage == maxPage) {
            Components.text("§8[§7->§8]")
        } else {
            Components.text("§8[§6->§8]")
                .hoverText(sender.asLangText("general-turn-to-next-page"))
                .clickRunCommand("$command ${currentPage + 1}")
        }
        return previous
            .append(" ")
            .append(pageOf)
            .append(" ")
            .append(next)
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
    }
}
