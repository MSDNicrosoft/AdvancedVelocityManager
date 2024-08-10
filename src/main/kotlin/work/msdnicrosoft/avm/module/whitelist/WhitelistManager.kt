package work.msdnicrosoft.avm.module.whitelist

import com.velocitypowered.api.util.UuidUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import taboolib.common.platform.function.*
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.FileUtil.json
import work.msdnicrosoft.avm.util.FileUtil.readTextWithBuffer
import work.msdnicrosoft.avm.util.FileUtil.writeTextWithBuffer
import work.msdnicrosoft.avm.util.StringUtil.toUuid
import work.msdnicrosoft.avm.util.UUIDUtil.toUndashedString
import work.msdnicrosoft.avm.util.data.UUIDSerializer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID
import kotlin.math.ceil
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@Suppress("TooManyFunctions")
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

    /**
     * Represents the response from the API lookup.
     *
     * @property id The UUID of the player.
     * @property name The name of the player.
     */
    @Serializable
    data class ApiResponse(val id: String, val name: String)

    enum class AddResult {
        SUCCESS,
        API_LOOKUP_NOT_FOUND,
        API_LOOKUP_REQUEST_FAILED,
        ALREADY_EXISTS,
        SAVE_FILE_FAILED,
    }

    enum class RemoveResult { SUCCESS, FAIL_NOT_FOUND, SAVE_FILE_FAILED }

    enum class WhitelistState { ON, OFF }

    private val lock = Object()

    private val file by lazy { getDataFolder().resolve("whitelist.json") }

    private lateinit var whitelist: MutableList<Player>

    private lateinit var usernames: HashSet<String>

    private lateinit var uuids: HashSet<UUID>

    private val config
        get() = ConfigManager.config.whitelist

    var state: WhitelistState
        get() = when (config.enabled) {
            true -> WhitelistState.ON
            false -> WhitelistState.OFF
        }
        set(value) {
            config.enabled = when (value) {
                WhitelistState.ON -> true
                WhitelistState.OFF -> false
            }
            ConfigManager.save()
        }

    val whitelistSize: Int
        get() = whitelist.size

    val whitelistIsEmpty: Boolean
        get() = whitelist.isEmpty()

    val maxPage: Int
        get() = ceil(whitelistSize.toInt() / 10F).toInt()

    /**
     * Represents whether the server is in online mode or not.
     *
     * This property is backed by the `isOnlineMode` property of the server's configuration.
     */
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
        updateCache()
    }

    fun onDisable() {
        saveWhitelist()
    }

    /**
     * Saves the whitelist to disk.
     *
     * @return True if the save was successful, false otherwise.
     */
    private fun saveWhitelist() = withLock {
        runCatching {
            file.writeTextWithBuffer(json.encodeToString(whitelist))
        }.onFailure { error("Failed to save whitelist: ${it.message}") }
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
                file.writeTextWithBuffer(json.encodeToString(listOf<Player>()))
            }.onFailure { error("Failed to initialize whitelist: ${it.message}") }
        }
        info("${if (reload) "Reloading" else "Loading"} whitelist...")
        withLock {
            whitelist = runCatching {
                json.decodeFromString<List<Player>>(file.readTextWithBuffer())
            }.getOrElse {
                error("Failed to load whitelist: ${it.message}")
                emptyList()
            }.toMutableList()
        }
    }

    /**
     * Updates the cache of usernames and UUIDs based on the provided parameters.
     *
     * @param username The username to add to the cache, or null to update the entire cache.
     * @param uuid The UUID to add to the cache, or null to update the entire cache.
     */
    fun updateCache(username: String? = null, uuid: UUID? = null) = withLock {
        if (username == null && uuid == null) {
            uuids = whitelist.map { it.uuid }.toHashSet()
            usernames = whitelist.map { it.name }.toHashSet()
        } else {
            if (uuid != null) uuids.add(uuid)
            if (username != null) usernames.add(username)
        }
    }

    /**
     * Adds a player to the whitelist if they are not already in it.
     * The player is identified either by their UUID or their username.
     * If the player is not found in the API lookup, the function returns the corresponding result.
     *
     * @param uuid The UUID of the player.
     * @param server The name of the server to which the player is being added.
     * @return An [AddResult] indicating the outcome of the operation.
     */
    fun add(uuid: UUID, server: String, onlineMode: Boolean? = null): AddResult = if (isInWhitelist(uuid)) {
        add(withLock { whitelist.find { it.uuid == uuid }!! }.name, uuid, server, onlineMode ?: serverIsOnlineMode)
    } else {
        when (val username = getUsername(uuid)) {
            null -> AddResult.API_LOOKUP_REQUEST_FAILED
            NOT_FOUND_RESULT -> AddResult.API_LOOKUP_NOT_FOUND
            else -> add(username, uuid, server, onlineMode ?: serverIsOnlineMode)
        }
    }

    /**
     * Adds a player to the whitelist if they are not already in it.
     * The player is identified by their username.
     * If the player is not found in the API lookup, the function returns the corresponding result.
     *
     * @param username The username of the player.
     * @param server The name of the server to which the player is being added.
     * @return An [AddResult] indicating the outcome of the operation.
     */
    fun add(username: String, server: String, onlineMode: Boolean? = null): AddResult = if (isInWhitelist(username)) {
        add(
            username,
            withLock { whitelist.find { it.name == username }!! }.uuid,
            server,
            onlineMode ?: serverIsOnlineMode
        )
    } else {
        when (val uuid = getUuid(username, onlineMode ?: serverIsOnlineMode)) {
            null -> AddResult.API_LOOKUP_REQUEST_FAILED
            NOT_FOUND_RESULT -> AddResult.API_LOOKUP_NOT_FOUND
            else -> add(username, uuid.toUuid(), server, onlineMode ?: serverIsOnlineMode)
        }
    }

    /**
     * Adds a player to the whitelist for a specific server.
     *
     * @param username The username of the player.
     * @param uuid The UUID of the player.
     * @param server The name of the server.
     * @return The result of the add operation.
     */
    fun add(username: String, uuid: UUID, server: String, onlineMode: Boolean): AddResult {
        // Check if the player is already in the server whitelist
        if (isInServerWhitelist(uuid, server)) return AddResult.ALREADY_EXISTS

        // Check if the player is already in the global whitelist
        if (!isInWhitelist(uuid)) {
            // Add the player to the global whitelist with the specified server
            withLock { whitelist.add(Player(username, uuid, onlineMode, listOf(server))) }
        } else {
            // Add the server to their server list
            withLock {
                whitelist.find { it.uuid == uuid }!!.apply {
                    serverList += server
                    this.onlineMode = onlineMode
                }
            }
        }
        updateCache(username = username, uuid = uuid)
        return if (saveWhitelist()) AddResult.SUCCESS else AddResult.SAVE_FILE_FAILED
    }

    /**
     * Removes a player from the whitelist for a specific server.
     *
     * @param username The username of the player to remove.
     * @param server The name of the server from which to remove the player.
     * If null, the player will be removed from the global whitelist.
     * @return The result of the remove operation.
     */
    fun remove(username: String, server: String?): RemoveResult =
        remove(withLock { whitelist.find { it.name == username }!! }, server)

    /**
     * Removes a player from the whitelist.
     *
     * @param uuid The UUID of the player to remove.
     * @param server The name of the server from which to remove the player.
     * If null, the player will be removed from the global whitelist.
     * @return The result of the remove operation.
     */
    fun remove(uuid: UUID, server: String?): RemoveResult =
        remove(withLock { whitelist.find { it.uuid == uuid }!! }, server)

    /**
     * Removes a player from the whitelist for a specific server.
     *
     * @param player The player to remove from the whitelist.
     * @param server The name of the server from which to remove the player.
     * If null, the player will be removed from the global whitelist.
     * @return The result of the remove operation.
     */
    fun remove(player: Player, server: String?): RemoveResult {
        withLock {
            // If a server is specified, check if it's in the player's server list
            if (player !in whitelist) return RemoveResult.FAIL_NOT_FOUND
            player.run {
                if (server != null) {
                    if (server !in serverList) return RemoveResult.FAIL_NOT_FOUND

                    // Remove the server from the player's server list
                    serverList -= server
                } else {
                    // Remove the player from the global whitelist
                    whitelist.remove(this)
                }
            }
        }
        updateCache()
        return if (saveWhitelist()) RemoveResult.SUCCESS else RemoveResult.SAVE_FILE_FAILED
    }

    /**
     * Clears the whitelist by removing all players from it.
     *
     * This function acquires a lock to ensure thread safety.
     * It then clears the whitelist by removing all players from it.
     * After that, it updates the cache to reflect the changes.
     * Finally, it saves the whitelist to disk and returns the result.
     *
     * @return `true` if the whitelist was successfully cleared and saved, `false` otherwise.
     */
    fun clear(): Boolean {
        withLock { whitelist.clear() }
        updateCache()
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
        val pages = withLock { whitelist.filter { username in it.name }.chunked(10) }
        return if (page > pages.size) emptyList() else pages[page - 1]
    }

    fun getPlayer(username: String) = withLock { whitelist.find { it.name == username } }

    fun getPlayer(uuid: UUID) = withLock { whitelist.find { it.uuid == uuid } }

    fun isInWhitelist(uuid: UUID): Boolean = withLock { uuid in uuids }

    fun isInWhitelist(username: String): Boolean = withLock { username in usernames }

    /**
     * Checks if a player with the given UUID is allowed to connect to a specific server.
     *
     * This function first checks if the player is in the whitelist. If not, it immediately returns false.
     * If the player is in the whitelist, it then checks if the server is in the list of allowed servers for the player.
     */
    fun isInServerWhitelist(uuid: UUID, server: String): Boolean = withLock {
        if (!isInWhitelist(uuid)) return false

        val serverList = whitelist.find { it.uuid == uuid }!!.serverList
        if (server in serverList) return true

        return config.serverGroups.any { (group, servers) ->
            group in serverList && server in servers
        }
    }

    /**
     * Retrieves the username associated with the given UUID.
     * If the server is in offline mode, returns null.
     * If the server is online, a query is made to the API to retrieve the username.
     *
     * @param uuid The UUID of the player.
     * @return The username associated with the UUID, or null if not found.
     */
    private fun getUsername(uuid: UUID): String? {
        if (!serverIsOnlineMode) return null

        val request = HttpRequest.newBuilder().uri(
            URI.create("${config.queryApi.profile.trimEnd('/')}/${uuid.toUndashedString()}")
        ).build()
        return try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).let { response ->
                when (response.statusCode()) {
                    204 -> NOT_FOUND_RESULT
                    !in 200..299 -> {
                        warning("Failed to query UUID $uuid, status code: ${response.statusCode()}")
                        null
                    }

                    else -> json.decodeFromString<ApiResponse>(response.body()).name
                }
            }
        } catch (e: Exception) {
            error("Failed to query UUID: ${e.message}")
            null
        }
    }

    /**
     * Retrieves the UUID associated with the given username.
     * If the server is in offline mode, an offline UUID is generated.
     * If the server is online, a query is made to the API to retrieve the UUID.
     * @param username The username of the player.
     * @return The UUID associated with the username, or null if not found.
     */
    private fun getUuid(username: String, onlineMode: Boolean): String? {
        if (!serverIsOnlineMode && !onlineMode) {
            return UuidUtils.generateOfflinePlayerUuid(username).toUndashedString()
        }

        val request = HttpRequest.newBuilder().uri(
            URI.create("${config.queryApi.uuid.trimEnd('/')}/$username")
        ).build()
        return try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).let { response ->
                when (response.statusCode()) {
                    404 -> NOT_FOUND_RESULT
                    !in 200..299 -> {
                        warning("Failed to query username $username, status code: ${response.statusCode()}")
                        null
                    }

                    else -> json.decodeFromString<ApiResponse>(response.body()).id
                }
            }
        } catch (e: Exception) {
            error("Failed to query username $username: ${e.message}")
            null
        }
    }

    fun updatePlayer(username: String, uuid: UUID) = submit(now = true) {
        withLock { whitelist.find { it.uuid == uuid }?.name = username }
        updateCache()
        saveWhitelist()
    }

    /**
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
