package work.msdnicrosoft.avm.util.command.context

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.JoinConfiguration
import work.msdnicrosoft.avm.util.component.builder.text.ComponentBuilder
import work.msdnicrosoft.avm.util.component.builder.text.component

inline val CommandSource.isConsole: Boolean get() = this is ConsoleCommandSource

inline val CommandSource.name: String get() = if (this is Player) this.username else "Console"

fun CommandSource.toPlayer(): Player = this as Player

inline fun CommandSource.sendMessage(
    joinConfiguration: JoinConfiguration = JoinConfiguration.noSeparators(),
    componentBuilder: ComponentBuilder.() -> Unit
) = this.sendMessage(component(joinConfiguration, componentBuilder))
