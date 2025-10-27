package work.msdnicrosoft.avm.util.component.widget

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.component.builder.text.component
import kotlin.math.ceil

class Paginator(private val command: String) {

    fun toComponent(currentPage: Int, maxPage: Int): Component = component(JoinConfiguration.spaces()) {
        componentLike(
            button("<-") {
                borderType(Button.BorderType.SQUARE)
                color {
                    enabled(NamedTextColor.GOLD)
                    disabled(NamedTextColor.GRAY)
                    border(NamedTextColor.DARK_GRAY)
                }
                enableWhen { currentPage != 1 }
                click { whenEnabled { runCommand("${this@Paginator.command} ${currentPage - 1}") } }
                hover { whenEnabled(tr("avm.general.page.previous")) }
            }
        )
        text("$currentPage/$maxPage") styled { color(NamedTextColor.AQUA) }
        componentLike(
            button("->") {
                borderType(Button.BorderType.SQUARE)
                color {
                    enabled(NamedTextColor.GOLD)
                    disabled(NamedTextColor.GRAY)
                    border(NamedTextColor.DARK_GRAY)
                }
                enableWhen { currentPage != maxPage }
                click { whenEnabled { runCommand("${this@Paginator.command} ${currentPage + 1}") } }
                hover { whenEnabled(tr("avm.general.page.next")) }
            }
        )
    }

    companion object {
        /**
         * The number of items to display per page.
         */
        const val ITEMS_PER_PAGE = 10

        /**
         * Calculates the maximum number of pages for the given [size].
         */
        fun getMaxPage(size: Int): Int = ceil(size.toFloat() / this.ITEMS_PER_PAGE.toFloat()).toInt()
    }
}
