@file:Suppress("NOTHING_TO_INLINE")

package work.msdnicrosoft.avm.util.server

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerPing
import net.kyori.adventure.text.Component
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin
import work.msdnicrosoft.avm.util.component.ComponentUtil
import java.util.Optional
import java.util.UUID
import java.util.concurrent.CompletableFuture

object ProxyServerUtil {

    /**
     * A server ping result with an unknown version and description.
     */
    val TIMEOUT_PING_RESULT: ServerPing = ServerPing.builder()
        .version(ServerPing.Version(-1, "Unknown"))
        .description(Component.text("Unknown"))
        .build()

    /**
     * Gets a registered server by its name.
     *
     * @param name The name of the server.
     * @return An optional containing the server if found, otherwise an empty optional.
     */
    inline fun getRegisteredServer(
        name: String
    ): Optional<RegisteredServer> = AdvancedVelocityManagerPlugin.Companion.server.getServer(
        name
    )

    /**
     * Gets a player by its username.
     *
     * @param name The username of the player.
     * @return An optional containing the player if found, otherwise an empty optional.
     */
    inline fun getPlayer(name: String): Optional<Player> = AdvancedVelocityManagerPlugin.Companion.server.getPlayer(
        name
    )

    /**
     * Gets a player by its UUID.
     *
     * @param uuid The UUID of the player.
     * @return An optional containing the player if found, otherwise an empty optional.
     */
    inline fun getPlayer(uuid: UUID): Optional<Player> = AdvancedVelocityManagerPlugin.Companion.server.getPlayer(uuid)

    /**
     * Checks if a server with the given name exists.
     *
     * @param name The name of the server.
     * @return Whether the server exists.
     */
    inline fun isValidRegisteredServer(name: String): Boolean = getRegisteredServer(name).isPresent

    /**
     * Checks if a player with the given username exists.
     *
     * @param username The username of the player.
     * @return Whether the player exists.
     */
    inline fun isValidPlayer(username: String): Boolean = getPlayer(username).isPresent

    /**
     * Checks if a player with the given UUID exists.
     *
     * @param uuid The UUID of the player.
     * @return Whether the player exists.
     */
    inline fun isValidPlayer(uuid: UUID): Boolean = getPlayer(uuid).isPresent

    /**
     * Kicks a list of players from the server.
     *
     * @param reason The reason for the kick.
     * @param players The players to kick.
     */
    inline fun kickPlayers(reason: String, vararg players: Player) {
        kickPlayers(reason, players.toList())
    }

    /**
     * Kicks a list of players from the server.
     *
     * @param reason The reason for the kick.
     * @param players The players to kick.
     */
    inline fun kickPlayers(reason: String, players: Iterable<Player>) {
        players.forEach { player ->
            player.disconnect(ComponentUtil.miniMessage.deserialize(reason))
        }
    }

    /**
     * Sends a player to a specific server.
     *
     * @param server The server to send the player to.
     * @param player The player to send.
     */
    inline fun sendPlayer(server: RegisteredServer, player: Player): CompletableFuture<Boolean> =
        player.createConnectionRequest(server).connectWithIndication()
}
