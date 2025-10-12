package work.msdnicrosoft.avm.module.whitelist.data

import kotlinx.serialization.Serializable

/**
 * The response from the API lookup.
 *
 * @property id The UUID of the player.
 * @property name The name of the player.
 */
@Serializable
data class ApiResponse(val id: String, val name: String)
