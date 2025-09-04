package work.msdnicrosoft.avm.util.component

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import java.net.URL

typealias S = CommandSource

fun tr(key: String, vararg args: ComponentLike): Component = Component.translatable(key, *args)

fun S.sendTranslatable(key: String, vararg args: ComponentLike) = this.sendMessage(Component.translatable(key, *args))

fun Component.hoverText(text: ComponentLike?): Component =
    if (text != null) this.hoverEvent(HoverEvent.showText(text)) else this

fun Component.hoverText(text: String?): Component =
    if (text != null) this.hoverEvent(HoverEvent.showText(Component.text(text))) else this

fun Component.clickToOpenUrl(url: String): Component = this.clickEvent(ClickEvent.openUrl(url))
fun Component.clickToOpenUrl(url: URL): Component = this.clickEvent(ClickEvent.openUrl(url))

fun Component.clickToRunCommand(command: String): Component = this.clickEvent(ClickEvent.runCommand(command))

fun Component.clickToSuggestCommand(command: String): Component = this.clickEvent(ClickEvent.suggestCommand(command))
