package work.msdnicrosoft.avm.util

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer

object ProxyServerUtil {
    /**
     * Kicks a list of players from the server.
     *
     * @param reason The reason for the kick.
     * @param players The players to kick.
     */
    fun kickPlayers(reason: String, vararg players: Player) = kickPlayers(reason, players.toList())

    /**
     * Kicks a list of players from the server.
     *
     * @param reason The reason for the kick.
     * @param players The players to kick.
     */
    fun kickPlayers(reason: String, players: Iterable<Player>) = players.forEach { player ->
        player.disconnect(ComponentUtil.serializer.parse(reason))
    }

    /**
     * Sends a player to a specific server.
     *
     * @param server The server to send the player to.
     * @param player The player to send.
     */
    fun sendPlayer(server: RegisteredServer, player: Player) =
        player.createConnectionRequest(server)
            .connectWithIndication()
}
