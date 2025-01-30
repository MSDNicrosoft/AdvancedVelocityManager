package work.msdnicrosoft.avm.module.whitelist

import com.velocitypowered.api.util.UuidUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.logger
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.FileUtil.json
import work.msdnicrosoft.avm.util.FileUtil.readTextWithBuffer
import work.msdnicrosoft.avm.util.FileUtil.writeTextWithBuffer
import work.msdnicrosoft.avm.util.HttpUtil
import work.msdnicrosoft.avm.util.StringUtil.toUuid
import work.msdnicrosoft.avm.util.data.UUIDSerializer
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID
import kotlin.math.ceil
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@Suppress("TooManyFunctions")
object WhitelistManager {

    private val config
        get() = ConfigManager.config.whitelist

    /**
     * This constant is used to indicate that a player was not found.
     */
    private const val NOT_FOUND_RESULT = "--NOT_FOUND--"

    private val lock = Object()

    private val httpClient = HttpClient.newHttpClient()

    private val file by lazy { getDataFolder().resolve("whitelist.json") }

    enum class AddResult {
        SUCCESS,
        API_LOOKUP_NOT_FOUND,
        API_LOOKUP_REQUEST_FAILED,
        ALREADY_EXISTS,
        SAVE_FILE_FAILED,
    }

    enum class RemoveResult { SUCCESS, FAIL_NOT_FOUND, SAVE_FILE_FAILED }

    enum class WhitelistState { ON, OFF }

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

    /**
     * Represents the response from the API lookup.
     *
     * @property id The UUID of the player.
     * @property name The name of the player.
     */
    @Serializable
    data class ApiResponse(val id: String, val name: String)

    private val whitelist = mutableListOf<Player>()

    val usernames = HashSet<String>()

    val uuids = HashSet<UUID>()

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

    val readOnlyWhitelist
        get() = withLock { whitelist.toList() }

    val size: Int
        get() = whitelist.size

    val isEmpty: Boolean
        get() = whitelist.isEmpty()

    val maxPage: Int
        get() = ceil(size.toFloat() / 10F).toInt()

    val serverIsOnlineMode: Boolean
        get() = AVM.plugin.server.configuration.isOnlineMode

    private inline fun <T> withLock(block: () -> T): T = synchronized(lock) { block() }

    /**
     * Called when the plugin is enabled.
     *
     * @param reload If true, the whitelist will be reloaded from disk.
     */
    fun onEnable(reload: Boolean = false) {
        load(reload)
        updateCache()
    }

    fun onDisable() {
        save()
    }

    /**
     * Saves the whitelist to disk.
     *
     * @return True if the save was successful, false otherwise.
     */
    private fun save(initialize: Boolean = false): Boolean {
        if (!file.exists()) {
            logger.info("Whitelist file does not exist{}", if (initialize) ", creating..." else "")
        }

        return try {
            file.parentFile.mkdirs()
            withLock {
                file.writeTextWithBuffer(json.encodeToString(if (initialize) listOf() else whitelist))
            }
            true
        } catch (e: IOException) {
            logger.error("Failed to save whitelist", e)
            false
        }
    }

    /**
     * Loads the whitelist from disk.
     *
     * @param reload If true, the whitelist will be reloaded from disk.
     */
    private fun load(reload: Boolean = false): Boolean {
        if (!file.exists()) return save(initialize = true)

        logger.info("{} whitelist...", if (reload) "Reloading" else "Loading")

        return try {
            withLock {
                whitelist.clear()
                whitelist.addAll(json.decodeFromString<List<Player>>(file.readTextWithBuffer()))
            }
            true
        } catch (e: IOException) {
            logger.error("Failed to read whitelist file", e)
            false
        } catch (e: SerializationException) {
            logger.error("Failed to decode whitelist from file", e)
            false
        }
    }

    /**
     * Updates the cache of usernames and UUIDs.
     */
    private fun updateCache() {
        withLock {
            uuids.clear()
            uuids.addAll(whitelist.map { it.uuid })

            usernames.clear()
            usernames.addAll(whitelist.map { it.name })
        }
    }

    /**
     * Adds a player to the whitelist with the specified UUID, server, and online mode.
     *
     * @param uuid The UUID of the player to add.
     * @param server The server to which the player will be added.
     * @param onlineMode The online mode of the player, or null if not specified.
     *
     * @return The result of the addition operation.
     */
    @Suppress("UnsafeCallOnNullableType")
    fun add(uuid: UUID, server: String, onlineMode: Boolean? = null): AddResult =
        if (isInWhitelist(uuid)) {
            add(getPlayer(uuid)!!.name, uuid, server, onlineMode)
        } else {
            when (val username = getUsername(uuid)) {
                null -> AddResult.API_LOOKUP_REQUEST_FAILED
                NOT_FOUND_RESULT -> AddResult.API_LOOKUP_NOT_FOUND
                else -> add(username, uuid, server, onlineMode)
            }
        }

    /**
     * Adds a player to the whitelist with the specified username and server.
     *
     * @param username The username of the player to add.
     * @param server The server to which the player will be added.
     * @param onlineMode The online mode of the player, or null if not specified.
     *
     * @return The result of the addition operation.
     */
    @Suppress("UnsafeCallOnNullableType")
    fun add(username: String, server: String, onlineMode: Boolean? = null): AddResult =
        if (isInWhitelist(username)) {
            add(username, getPlayer(username)!!.uuid, server, onlineMode)
        } else {
            when (val uuid = getUuid(username, onlineMode)) {
                null -> AddResult.API_LOOKUP_REQUEST_FAILED
                NOT_FOUND_RESULT -> AddResult.API_LOOKUP_NOT_FOUND
                else -> add(username, uuid.toUuid(), server, onlineMode)
            }
        }

    /**
     * Adds a player to the whitelist with the specified username, UUID, server, and online mode.
     *
     * If the player is not already in the global whitelist, they are added with the specified server.
     * If the player is already in the global whitelist, the specified server is added to their server list.
     *
     * @param username The username of the player to add.
     * @param uuid The UUID of the player to add.
     * @param server The server to which the player will be added.
     * @param onlineMode The online mode of the player, or null if not specified.
     *
     * @return The result of the addition operation.
     */
    @Suppress("UnsafeCallOnNullableType")
    fun add(username: String, uuid: UUID, server: String, onlineMode: Boolean?): AddResult {
        // Check if the player is already in the server whitelist
        if (isInServerWhitelist(uuid, server)) return AddResult.ALREADY_EXISTS

        // Check if the player is already in the global whitelist
        if (!isInWhitelist(uuid)) {
            // Add the player to the global whitelist with the specified server
            withLock { whitelist.add(Player(username, uuid, onlineMode ?: serverIsOnlineMode, listOf(server))) }
        } else {
            // Add the server to their server list
            getPlayer(uuid)!!.apply {
                withLock {
                    serverList += server
                    if (onlineMode != null) {
                        this.onlineMode = onlineMode
                    }
                }
            }
        }
        withLock {
            uuids.add(uuid)
            usernames.add(username)
        }
        return if (save()) AddResult.SUCCESS else AddResult.SAVE_FILE_FAILED
    }

    /**
     * Removes a player from the whitelist for a specific server.
     *
     * @param username The username of the player to remove.
     * @param server The name of the server from which to remove the player.
     * If null, the player will be removed from the global whitelist.
     * @return The result of the remove operation.
     */
    @Suppress("UnsafeCallOnNullableType")
    fun remove(username: String, server: String?): RemoveResult =
        remove(getPlayer(username)!!, server)

    /**
     * Removes a player from the whitelist.
     *
     * @param uuid The UUID of the player to remove.
     * @param server The name of the server from which to remove the player.
     * If null, the player will be removed from the global whitelist.
     * @return The result of the remove operation.
     */
    @Suppress("UnsafeCallOnNullableType")
    fun remove(uuid: UUID, server: String?): RemoveResult =
        remove(getPlayer(uuid)!!, server)

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

            if (server != null) {
                if (server !in player.serverList) return RemoveResult.FAIL_NOT_FOUND

                // Remove the server from the player's server list
                player.serverList -= server
            } else {
                // Remove the player from the global whitelist
                whitelist.remove(player)
            }
        }
        updateCache()
        return if (save()) RemoveResult.SUCCESS else RemoveResult.SAVE_FILE_FAILED
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
        return save()
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

    /**
     * Finds a player in the whitelist by their UUID.
     */
    fun getPlayer(username: String): Player? = withLock { whitelist.find { it.name == username } }

    /**
     * Finds a player in the whitelist by their UUID.
     */
    fun getPlayer(uuid: UUID): Player? = withLock { whitelist.find { it.uuid == uuid } }

    /**
     * Checks if a player with the given identifier is in the whitelist.
     */
    fun isInWhitelist(uuid: UUID): Boolean = withLock { uuid in uuids }

    /**
     * Checks if a player with the given identifier is in the whitelist.
     */
    fun isInWhitelist(username: String): Boolean = withLock { username in usernames }

    /**
     * Checks if a player with the given UUID is allowed to connect to a specific server.
     *
     * This function first checks if the player is in the whitelist. If not, it immediately returns false.
     * If the player is in the whitelist, it then checks if the server is in the list of allowed servers for the player.
     */
    @Suppress("UnsafeCallOnNullableType")
    fun isInServerWhitelist(uuid: UUID, server: String): Boolean = withLock {
        if (!isInWhitelist(uuid)) return false

        val serverList = getPlayer(uuid)!!.serverList
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
     * @return The username associated with the UUID, or null if the query fails.
     */
    @Suppress("MagicNumber")
    private fun getUsername(uuid: UUID): String? {
        if (!serverIsOnlineMode) return null

        val request = HttpRequest.newBuilder()
            .setHeader("User-Agent", HttpUtil.USER_AGENT)
            .uri(URI.create("${config.queryApi.profile.trimEnd('/')}/${UuidUtils.toUndashed(uuid)}"))
            .build()

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .handleAsync { response, throwable ->
                if (throwable != null) {
                    logger.warn("Failed to query UUID {}: {}", uuid, throwable.message)
                    null
                } else {
                    when (val statusCode = response.statusCode()) {
                        200 -> json.decodeFromString<ApiResponse>(response.body()).name
                        404, 204 -> NOT_FOUND_RESULT

                        else -> {
                            if (statusCode == 429) {
                                logger.warn("Exceeded to the rate limit of Profile API, please retry UUID {}", uuid)
                            } else {
                                logger.warn("Failed to query UUID {}, status code: {}", uuid, statusCode)
                            }
                            null
                        }
                    }
                }
            }.get()
    }

    /**
     * Retrieves the UUID associated with the given username.
     *
     * @param username The username to query for its UUID.
     * @param onlineMode Optional parameter to specify whether to use online mode or not. Defaults to null.
     * @return The UUID associated with the username, or null if the query fails.
     */
    @Suppress("MagicNumber")
    private fun getUuid(username: String, onlineMode: Boolean? = null): String? {
        if (onlineMode == false && !serverIsOnlineMode) {
            return UuidUtils.toUndashed(UuidUtils.generateOfflinePlayerUuid(username))
        }

        val request = HttpRequest.newBuilder()
            .setHeader("User-Agent", HttpUtil.USER_AGENT)
            .uri(URI.create("${config.queryApi.uuid.trimEnd('/')}/$username"))
            .build()

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .handleAsync { response, throwable ->
                if (throwable != null) {
                    logger.warn("Failed to query username {}: {}", username, throwable.message)
                    null
                } else {
                    when (val statusCode = response.statusCode()) {
                        200 -> json.decodeFromString<ApiResponse>(response.body()).id
                        404 -> NOT_FOUND_RESULT

                        else -> {
                            if (statusCode == 429) {
                                logger.warn(
                                    "Exceeded to the rate limit of UUID API, please retry username {}",
                                    username
                                )
                            } else {
                                logger.warn("Failed to query username {}, status code: {}", username, statusCode)
                            }

                            null
                        }
                    }
                }
            }.get()
    }

    fun updatePlayer(username: String, uuid: UUID) = submit(now = true) {
        withLock { whitelist.find { it.uuid == uuid }?.name = username }
        updateCache()
        save()
    }

    /**
     * Retrieves a paged version of the whitelist.
     * @param page The page number to retrieve.
     * @return A list of players on the specified page.
     */
    fun getPagedWhitelist(page: Int): List<Player> {
        val pages = withLock { readOnlyWhitelist.chunked(10) }
        return pages[page - 1]
    }
}
