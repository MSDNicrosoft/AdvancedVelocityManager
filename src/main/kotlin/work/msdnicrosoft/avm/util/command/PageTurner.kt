package work.msdnicrosoft.avm.util.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.lang.asLangText

class PageTurner(val sender: ProxyCommandSender, val command: String) {

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
        return Components.empty()
            .append(previous)
            .append(" ")
            .append(pageOf)
            .append(" ")
            .append(next)
    }
}
