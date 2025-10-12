package work.msdnicrosoft.avm.util.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import work.msdnicrosoft.avm.util.component.ComponentSerializer.MINI_MESSAGE
import work.msdnicrosoft.avm.util.component.clickToRunCommand
import work.msdnicrosoft.avm.util.component.hoverText
import work.msdnicrosoft.avm.util.component.tr
import kotlin.math.ceil

/**
 * Pagination helper for commands that need to display a list of items spread across multiple pages.
 *
 * @param command The command name for the pagination component.
 */
class PageTurner(val command: String) {

    /**
     * Builds navigation buttons and a page indicator with [currentPage] and [maxPage].
     */
    fun build(currentPage: Int, maxPage: Int): Component =
        Component.join(
            JoinConfiguration.spaces(),
            if (currentPage == 1) {
                navigationButton("<gray><-")
            } else {
                navigationButton("<gold><-")
                    .hoverText(tr("avm.general.page.previous"))
                    .clickToRunCommand("${this.command} ${currentPage - 1}")
            },
            MINI_MESSAGE.deserialize("<aqua>$currentPage/$maxPage"),
            if (currentPage == maxPage) {
                navigationButton("<gray>->")
            } else {
                navigationButton("<gold>->")
                    .hoverText(tr("avm.general.page.next"))
                    .clickToRunCommand("${this.command} ${currentPage + 1}")
            }
        )

    companion object {
        /**
         * The number of items to display per page.
         */
        const val ITEMS_PER_PAGE = 10

        /**
         * Calculates the maximum number of pages for the given [size].
         */
        fun getMaxPage(size: Int): Int = ceil(size.toFloat() / this.ITEMS_PER_PAGE.toFloat()).toInt()

        private fun navigationButton(arrow: String): Component =
            MINI_MESSAGE.deserialize("<dark_gray>[$arrow<dark_gray>]")
    }
}
