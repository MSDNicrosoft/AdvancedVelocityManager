package work.msdnicrosoft.avm.util

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerPing
import net.kyori.adventure.text.Component
import work.msdnicrosoft.avm.util.StringUtil.formated

object ProxyServerUtil {

    /**
     * A server ping result with unknown version and description.
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
    fun kickPlayers(reason: String, vararg players: Player) = kickPlayers(reason, players.toList())

    /**
     * Kicks a list of players from the server.
     *
     * @param reason The reason for the kick.
     * @param players The players to kick.
     */
    fun kickPlayers(reason: String, players: Iterable<Player>) = players.forEach { player ->
        player.disconnect(reason.formated())
    }

    /**
     * Sends a player to a specific server.
     *
     * @param server The server to send the player to.
     * @param player The player to send.
     */
    fun sendPlayer(server: RegisteredServer, player: Player) =
        player.createConnectionRequest(server).connectWithIndication()

    fun Player.sendMessage(message: String) = sendMessage(message.formated())
}
