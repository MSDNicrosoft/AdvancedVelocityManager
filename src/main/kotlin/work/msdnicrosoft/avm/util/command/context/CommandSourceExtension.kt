@file:Suppress("unused", "NOTHING_TO_INLINE")

package work.msdnicrosoft.avm.util.command.context

import com.velocitypowered.api.proxy.ConsoleCommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.client.ConnectedPlayer

typealias Console = ConsoleCommandSource

inline val S.isConsole: Boolean
    get() = this is Console

inline val S.isPlayer: Boolean
    get() = this is Player

inline val S.name: String
    get() = if (this is Player) this.username else "Console"

inline fun S.toPlayer(): Player = this as Player
inline fun S.toConsole(): Console = this as Console
inline fun S.toConnectedPlayer(): ConnectedPlayer = this as ConnectedPlayer
