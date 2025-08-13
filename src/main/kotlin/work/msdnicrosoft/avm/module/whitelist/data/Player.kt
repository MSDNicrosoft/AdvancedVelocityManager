package work.msdnicrosoft.avm.module.whitelist.data

import kotlinx.serialization.Serializable
import work.msdnicrosoft.avm.util.data.UUIDSerializer
import java.util.UUID

/**
 * Represents a player in the whitelist.
 *
 * @property name The name of the player.
 * @property uuid The UUID of the player.
 * @property onlineMode Whether the player is online mode
 * @property serverList The list of servers the player is allowed to connect to.
 */
@Serializable
data class Player(
    var name: String,
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    var onlineMode: Boolean,
    var serverList: List<String>
)
