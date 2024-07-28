package work.msdnicrosoft.avm.module.whitelist

import com.velocitypowered.api.util.UuidUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import taboolib.common.platform.function.*
import work.msdnicrosoft.avm.util.ConfigUtil.json
import work.msdnicrosoft.avm.util.Extensions.toUndashedString
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID
import kotlin.math.ceil
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

object WhitelistManager {

    /**
     * This constant is used to indicate that a player was not found.
     */
    private const val NOT_FOUND_RESULT = "--NOT_FOUND--"

    /**
     * Represents a player in the whitelist.
     *
     * @property name The name of the player.
     * @property uuid The UUID of the player.
     */
    @Serializable
    data class Player(var name: String, var uuid: String)

    /**
     * Represents the response from the API lookup.
     *
     * @property id The UUID of the player.
     * @property name The name of the player.
     */
    @Serializable
    data class ApiResponse(val id: String, val name: String)

    /**
     * Represents the result of adding a player to the whitelist.
     */
    enum class AddResult {
        /**
         * The player was successfully added to the whitelist.
         */
        SUCCESS,

        /**
         * The player was not found in the API lookup.
         */
        API_LOOKUP_NOT_FOUND,

        /**
         * The API lookup failed.
         */
        API_LOOKUP_REQUEST_FAILED,

        /**
         * The player is already in the whitelist.
         */
        ALREADY_EXISTS,

        /**
         * Failed to save the whitelist file.
         */
        SAVE_FILE_FAILED,
    }

    /**
     * Represents the result of removing a player from the whitelist.
     */
    enum class RemoveResult {

        /**
         * The player was successfully removed from the whitelist.
         */
        SUCCESS,

        /**
         * The player was not found in the whitelist.
         */
        FAIL_NOT_FOUND,

        /**
         * Failed to save the whitelist file.
         */
        SAVE_FILE_FAILED
    }

    /**
     * Represents the state of the whitelist.
     */
    enum class WhitelistState { ON, OFF }

    private val lock = Object()

    /**
     * The file where the whitelist is stored.
     */
    private val file by lazy {
        getDataFolder().resolve(
            if (AVM.plugin.server.configuration.isOnlineMode) {
                "whitelist.json"
            } else {
                "whitelist_offline.json"
            }
        )
    }

    private lateinit var whitelist: MutableList<Player>

    /**
     * Gets and sets the state of the whitelist.
     */
    var state: WhitelistState
        get() = AVM.withLock {
            when (AVM.config.whitelist.enabled) {
                true -> WhitelistState.ON
                false -> WhitelistState.OFF
            }
        }
        set(value) = AVM.withLock {
            AVM.config.whitelist.enabled = when (value) {
                WhitelistState.ON -> true
                WhitelistState.OFF -> false
            }
            AVM.saveConfig()
        }

    /**
     * @property whitelistSize size of the whitelist.
     */
    val whitelistSize: Int
        get() = whitelist.size

    /**
     * @property whitelistIsEmpty Indicates whether the whitelist is empty or not.
     */
    val whitelistIsEmpty: Boolean
        get() = whitelist.isEmpty()

    /**
     * @property maxPage maximum page of the whitelist.
     */
    val maxPage: Int
        get() = ceil(whitelistSize.toInt() / 10F).toInt()

    val serverIsOnlineMode: Boolean
        get() = AVM.plugin.server.configuration.isOnlineMode

    /**
     * Executes the given block of code while holding a lock on the `lock` object.
     *
     * @param block The block of code to execute.
     * @return The result of the block of code.
     */
    private inline fun <T> withLock(block: () -> T) = synchronized(lock) { block() }

    /**
     * Called when the plugin is enabled.
     *
     * @param reload If true, the whitelist will be reloaded from disk.
     */
    fun onEnable(reload: Boolean = false) {
        loadWhitelist(reload)
    }

    /**
     * Called when the plugin is disabled.
     */
    fun onDisable() {
        saveWhitelist()
    }

    /**
     * Saves the whitelist to disk.
     *
     * @return True if the save was successful, false otherwise.
     */
    private fun saveWhitelist() = withLock {
        runCatching { file.writeText(json.encodeToString(whitelist)) }
            .onFailure { error("Failed to save whitelist: ${it.message}") }
    }.isSuccess

    /**
     * Loads the whitelist from disk.
     *
     * @param reload If true, the whitelist will be reloaded from disk.
     */
    private fun loadWhitelist(reload: Boolean = false) {
        if (!file.exists()) {
            runCatching {
                info("Whitelist file does not exist, creating...")
                file.parentFile.mkdirs()
                file.writeText(json.encodeToString(listOf<Player>()))
            }.onFailure { error("Failed to initialize whitelist: ${it.message}") }
        }
        info("${if (reload) "Reloading" else "Loading"} whitelist...")
        withLock {
            whitelist = runCatching {
                json.decodeFromString<List<Player>>(file.readText())
            }.getOrElse {
                error("Failed to load whitelist: ${it.message}")
                emptyList()
            }.toMutableList()
        }
    }

    /**
     * Adds a player to the whitelist.
     *
     * @param uuid The UUID of the player to add.
     * @return The result of the addition operation.
     */
    fun add(uuid: UUID) = when (val name = getUsername(uuid)) {
        null -> AddResult.API_LOOKUP_REQUEST_FAILED
        NOT_FOUND_RESULT -> AddResult.API_LOOKUP_NOT_FOUND
        else -> add(Player(name, uuid.toUndashedString()))
    }

    /**
     * Adds a player to the whitelist.
     *
     * @param username The username of the player to add.
     * @return The result of the addition operation.
     */
    fun add(username: String) = when (val uuid = getUuid(username)) {
        null -> AddResult.API_LOOKUP_REQUEST_FAILED
        NOT_FOUND_RESULT -> AddResult.API_LOOKUP_NOT_FOUND
        else -> add(Player(username, uuid))
    }

    /**
     * Adds a player to the whitelist.
     *
     * @param player The player to add.
     * @return The result of the addition operation.
     */
    fun add(player: Player) = if (isWhitelisted(player)) {
        AddResult.ALREADY_EXISTS
    } else {
        withLock { whitelist.add(player) }
        if (saveWhitelist()) {
            AddResult.SUCCESS
        } else {
            AddResult.SAVE_FILE_FAILED
        }
    }

    /**
     * Removes a player from the whitelist by their username.
     *
     * @param username The username of the player to remove.
     * @return The result of the removal operation.
     */
    fun remove(username: String): RemoveResult {
        val success = withLock { whitelist.removeIf { it.name == username } }
        return if (success) {
            if (saveWhitelist()) {
                RemoveResult.SUCCESS
            } else {
                RemoveResult.SAVE_FILE_FAILED
            }
        } else {
            RemoveResult.FAIL_NOT_FOUND
        }
    }

    /**
     * Removes a player from the whitelist by their UUID.
     *
     * @param uuid The UUID of the player to remove.
     * @return The result of the removal operation.
     */
    fun remove(uuid: UUID): RemoveResult {
        val success = withLock { whitelist.removeIf { it.uuid == uuid.toUndashedString() } }
        return if (success) {
            if (saveWhitelist()) {
                RemoveResult.SUCCESS
            } else {
                RemoveResult.SAVE_FILE_FAILED
            }
        } else {
            RemoveResult.FAIL_NOT_FOUND
        }
    }

    /**
     * Clears the whitelist by removing all players from it.
     *
     * @return True if the whitelist was successfully cleared and saved, false otherwise.
     */
    fun clear(): Boolean {
        withLock { whitelist.clear() }
        return saveWhitelist()
    }

    /**
     * Finds players in the whitelist by their username and returns them in pages.
     *
     * @param username The username to search for.
     * @param page The page number to return.
     * @return A list of players matching the search criteria.
     */
    fun find(username: String, page: Int): List<Player> {
        val pages = withLock {
            whitelist.filter { username in it.name }.chunked(10)
        }
        return if (page > pages.size) emptyList() else pages[page - 1]
    }

    /**
     * Checks if a player is whitelisted by their username.
     *
     * @param player The player to check.
     * @return True if the player is whitelisted, false otherwise.
     */
    fun isWhitelisted(player: Player): Boolean = withLock { player in whitelist }

    /**
     * Checks if a player is whitelisted by their username.
     *
     * @param username The username of the player to check.
     * @return True if the player is whitelisted, false otherwise.
     */
    fun isWhitelisted(username: String): Boolean = withLock { whitelist.any { username == it.name } }

    /**
     * Retrieves the username associated with the given UUID.
     * @param uuid The UUID of the player.
     * @return The username associated with the UUID, or null if not found.
     */
    private fun getUsername(uuid: UUID): String? {
        if (!serverIsOnlineMode) return null

        val request = HttpRequest.newBuilder().uri(
            URI.create("${AVM.config.whitelist.queryApi.profile.trimEnd('/')}/${uuid.toUndashedString()}")
        ).build()
        return try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).let { response ->
                when (response.statusCode()) {
                    204 -> NOT_FOUND_RESULT
                    !in 200..299 -> {
                        warning(
                            "An error occurred while querying UUID $uuid, status code: ${response.statusCode()}"
                        )
                        null
                    }

                    else -> json.decodeFromString<ApiResponse>(response.body()).name
                }
            }
        } catch (e: Exception) {
            error("An error occurred while querying uuid: ${e.message}")
            null
        }
    }

    /**
     * Retrieves the UUID associated with the given username.
     * @param username The username of the player.
     * @return The UUID associated with the username, or null if not found.
     */
    private fun getUuid(username: String): String? {
        if (!serverIsOnlineMode) {
            return UuidUtils.generateOfflinePlayerUuid(username).toUndashedString()
        }

        val request = HttpRequest.newBuilder().uri(
            URI.create("${AVM.config.whitelist.queryApi.uuid.trimEnd('/')}/$username")
        ).build()
        return try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).let { response ->
                when (response.statusCode()) {
                    404 -> NOT_FOUND_RESULT
                    !in 200..299 -> {
                        warning(
                            "An error occurred while querying username $username, status code: ${response.statusCode()}"
                        )
                        return null
                    }

                    else -> json.decodeFromString<ApiResponse>(response.body()).id
                }
            }
        } catch (e: Exception) {
            error("An error occurred while querying username $username: ${e.message}")
            null
        }
    }

    /**
     * Retrieves the current whitelist.
     * @return A copy of the current whitelist.
     */
    fun getWhitelist() = withLock { whitelist.toList() }

    /**
     * Retrieves a paged version of the whitelist.
     * @param page The page number to retrieve.
     * @return A list of players on the specified page.
     */
    fun getPagedWhitelist(page: Int): List<Player> {
        val pages = withLock { getWhitelist().chunked(10) }
        return pages[page - 1]
    }
}
