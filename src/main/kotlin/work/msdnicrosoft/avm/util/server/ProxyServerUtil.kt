package work.msdnicrosoft.avm.util.server

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.ServerPing
import net.kyori.adventure.text.Component
import work.msdnicrosoft.avm.util.component.builder.minimessage.miniMessage

object ProxyServerUtil {
    /**
     * A server ping result with an unknown version and description.
     */
    val TIMEOUT_PING_RESULT: ServerPing = ServerPing.builder()
        .version(ServerPing.Version(-1, "Unknown"))
        .description(Component.text("Unknown"))
        .build()

    /**
     * Kicks a list of [players] from the server with a specified [reason].
     */
    fun kickPlayers(reason: String, vararg players: Player) {
        kickPlayers(reason, players.toList())
    }

    /**
     * Kicks a list of [players] from the server with a specified [reason].
     */
    fun kickPlayers(reason: String, players: Iterable<Player>) {
        players.forEach { player ->
            player.disconnect(miniMessage(reason))
        }
    }
}
