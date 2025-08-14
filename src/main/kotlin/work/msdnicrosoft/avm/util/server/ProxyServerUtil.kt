package work.msdnicrosoft.avm.util.server

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.ServerPing
import net.kyori.adventure.text.Component
import work.msdnicrosoft.avm.util.component.ComponentSerializer.MINI_MESSAGE

object ProxyServerUtil {
    /**
     * A server ping result with an unknown version and description.
     */
    val TIMEOUT_PING_RESULT: ServerPing = ServerPing.builder()
        .version(ServerPing.Version(-1, "Unknown"))
        .description(Component.text("Unknown"))
        .build()

    /**
     * Kicks a list of players from the server.
     *
     * @param reason The reason for the kick.
     * @param players The players to kick.
     */
    fun kickPlayers(reason: String, vararg players: Player) {
        kickPlayers(reason, players.toList())
    }

    /**
     * Kicks a list of players from the server.
     *
     * @param reason The reason for the kick.
     * @param players The players to kick.
     */
    fun kickPlayers(reason: String, players: Iterable<Player>) {
        players.forEach { player ->
            player.disconnect(MINI_MESSAGE.deserialize(reason))
        }
    }
}
