package work.msdnicrosoft.avm.util.component

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

typealias S = CommandSource

fun tr(key: String, vararg args: ComponentLike): Component = Component.translatable(key, *args)

fun S.sendTranslatable(key: String, vararg args: ComponentLike) = this.sendMessage(Component.translatable(key, *args))
