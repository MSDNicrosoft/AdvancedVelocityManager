@file:Suppress("NOTHING_TO_INLINE")

package work.msdnicrosoft.avm.util.component

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

inline fun tr(key: String, vararg args: ComponentLike): Component = Component.translatable(key, *args)

inline fun CommandSource.sendTranslatable(key: String, vararg args: ComponentLike) =
    this.sendMessage(Component.translatable(key, *args))
