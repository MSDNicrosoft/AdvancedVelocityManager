package work.msdnicrosoft.avm.module.whitelist

import com.velocitypowered.api.util.UuidUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.eventManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.HttpUtil
import work.msdnicrosoft.avm.util.command.PageTurner
import work.msdnicrosoft.avm.util.data.UUIDSerializer
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import work.msdnicrosoft.avm.util.file.writeTextWithBuffer
import work.msdnicrosoft.avm.util.server.task
import work.msdnicrosoft.avm.util.string.toUuid
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.io.path.div

@Suppress("TooManyFunctions")
object WhitelistManager {

    enum class AddResult {
        SUCCESS,
        API_LOOKUP_NOT_FOUND,
        API_LOOKUP_REQUEST_FAILED,
        ALREADY_EXISTS,
        SAVE_FILE_FAILED,
    }

    enum class RemoveResult { SUCCESS, FAIL_NOT_FOUND, SAVE_FILE_FAILED }

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

    private inline val config
        get() = ConfigManager.config.whitelist

    /**
     * This constant is used to indicate that a player was not found.
     */
    private const val NOT_FOUND_RESULT = "--NOT_FOUND--"

    private val lock = ReentrantReadWriteLock()

    private val httpClient = HttpClient.newHttpClient()

    private val file = (dataDirectory / "whitelist.json").toFile()

    private val whitelist = mutableListOf<Player>()

    val usernames = hashSetOf<String>()

    val uuids = hashSetOf<UUID>()

    var enabled: Boolean
        get() = config.enabled
        set(value) {
            config.enabled = value
            ConfigManager.save()
        }

    val size: Int
        get() = whitelist.size

    val isEmpty: Boolean
        get() = whitelist.isEmpty()

    val maxPage: Int
        get() = PageTurner.getMaxPage(whitelist.size)

    inline val serverIsOnlineMode: Boolean
        get() = server.configuration.isOnlineMode

    /**
     * Called when the plugin is enabled.
     *
     * @param reload If true, the whitelist will be reloaded from the disk.
     */
    fun init(reload: Boolean = false) {
        load(reload)
        updateCache()
        eventManager.register(plugin, WhitelistHandler)
    }

    fun disable(): Boolean {
        eventManager.unregisterListener(plugin, WhitelistHandler)
        return save()
    }

    fun reload() {
        eventManager.unregisterListener(plugin, WhitelistHandler)
        init(reload = true)
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
            lock.read { file.writeTextWithBuffer(JSON.encodeToString(if (initialize) listOf() else whitelist)) }
            true
        } catch (e: IOException) {
            logger.error("Failed to save whitelist", e)
            false
        }
    }

    /**
     * Loads the whitelist from disk.
     *
     * @param reload If true, the whitelist will be reloaded from the disk.
     */
    private fun load(reload: Boolean = false): Boolean {
        if (!file.exists()) return save(initialize = true)

        logger.info("{} whitelist...", if (reload) "Reloading" else "Loading")

        return try {
            lock.write {
                whitelist.clear()
                whitelist.addAll(JSON.decodeFromString<List<Player>>(file.readTextWithBuffer()))
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
        lock.write {
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
    fun add(uuid: UUID, server: String, onlineMode: Boolean? = null): AddResult {
        val player = getPlayer(uuid)
        return if (player != null) {
            add(player.name, uuid, server, onlineMode)
        } else {
            when (val username = getUsername(uuid)) {
                null -> AddResult.API_LOOKUP_REQUEST_FAILED
                NOT_FOUND_RESULT -> AddResult.API_LOOKUP_NOT_FOUND
                else -> add(username, uuid, server, onlineMode)
            }
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
    fun add(username: String, server: String, onlineMode: Boolean? = null): AddResult {
        val player = getPlayer(username)
        return if (player != null) {
            add(username, player.uuid, server, onlineMode)
        } else {
            when (val uuid = getUuid(username, onlineMode)) {
                null -> AddResult.API_LOOKUP_REQUEST_FAILED
                NOT_FOUND_RESULT -> AddResult.API_LOOKUP_NOT_FOUND
                else -> add(username, uuid.toUuid(), server, onlineMode)
            }
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
    fun add(username: String, uuid: UUID, server: String, onlineMode: Boolean?): AddResult {
        val player = getPlayer(uuid)
        lock.write {
            if (player == null) {
                whitelist.add(Player(username, uuid, onlineMode ?: serverIsOnlineMode, mutableListOf(server)))
                uuids.add(uuid)
                usernames.add(username)
            } else {
                // Check if the player is already in the server whitelist
                if (server !in player.serverList) {
                    player.serverList += server
                } else {
                    if (onlineMode == player.onlineMode) return AddResult.ALREADY_EXISTS
                }
                if (onlineMode != null) player.onlineMode = onlineMode
            }
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
    fun remove(username: String, server: String?): RemoveResult {
        val player = getPlayer(username) ?: return RemoveResult.FAIL_NOT_FOUND
        return remove(player, server)
    }

    /**
     * Removes a player from the whitelist.
     *
     * @param uuid The UUID of the player to remove.
     * @param server The name of the server from which to remove the player.
     * If null, the player will be removed from the global whitelist.
     * @return The result of the remove operation.
     */
    fun remove(uuid: UUID, server: String?): RemoveResult {
        val player = getPlayer(uuid) ?: return RemoveResult.FAIL_NOT_FOUND
        return remove(player, server)
    }

    /**
     * Removes a player from the whitelist for a specific server.
     *
     * @param player The player to remove from the whitelist.
     * @param server The name of the server from which to remove the player.
     * If null, the player will be removed from the global whitelist.
     * @return The result of the remove operation.
     */
    fun remove(player: Player, server: String?): RemoveResult {
        lock.write {
            if (server != null) {
                if (server !in player.serverList) return RemoveResult.FAIL_NOT_FOUND

                // Remove the server from the player's server list
                player.serverList -= server

                // If the server list is now empty, remove the player from the global whitelist
                if (player.serverList.isEmpty()) whitelist.remove(player)
            } else {
                // Remove the player from the global whitelist
                whitelist.remove(player)
            }
            uuids.remove(player.uuid)
            usernames.remove(player.name)
        }
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
        lock.write {
            whitelist.clear()
            uuids.clear()
            usernames.clear()
        }
        return save()
    }

    /**
     * Finds players in the whitelist by their username and returns them in pages.
     *
     * @param keyword The keyword to search for.
     * @param page The page number to return.
     * @return A list of players matching the search criteria.
     */
    fun find(keyword: String, page: Int): List<Player> =
        lock.read { whitelist.filter { keyword in it.name } }
            .chunked(PageTurner.ITEMS_PER_PAGE)
            .getOrNull(page - 1)
            .orEmpty()

    /**
     * Finds a player in the whitelist by their UUID.
     */
    fun getPlayer(username: String): Player? = lock.read { whitelist.find { it.name == username } }

    /**
     * Finds a player in the whitelist by their UUID.
     */
    fun getPlayer(uuid: UUID): Player? = lock.read { whitelist.find { it.uuid == uuid } }

    /**
     * Checks if a player with the given UUID is allowed to connect to a specific server.
     *
     * This function first checks if the player is in the whitelist. If not, it immediately returns false.
     * If the player is in the whitelist, it then checks if the server is in the list of allowed servers for the player.
     */
    fun isInServerWhitelist(uuid: UUID, server: String): Boolean = lock.read {
        val player = whitelist.find { it.uuid == uuid } ?: return false

        val serverList = player.serverList
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
            .thenApply { response ->
                when (val statusCode = response.statusCode()) {
                    200 -> JSON.decodeFromString<ApiResponse>(response.body()).name
                    404, 204 -> NOT_FOUND_RESULT
                    429 -> {
                        logger.warn("Exceeded to the rate limit of Profile API, please retry UUID {}", uuid)
                        null
                    }

                    else -> {
                        logger.warn("Failed to query UUID {}, status code: {}", uuid, statusCode)
                        null
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
            .thenApply { response ->
                when (val statusCode = response.statusCode()) {
                    200 -> JSON.decodeFromString<ApiResponse>(response.body()).id
                    404 -> NOT_FOUND_RESULT
                    429 -> {
                        logger.warn("Exceeded to the rate limit of UUID API, please retry username {}", username)
                        null
                    }

                    else -> {
                        logger.warn("Failed to query username {}, status code: {}", username, statusCode)
                        null
                    }
                }
            }.get()
    }

    fun updatePlayer(username: String, uuid: UUID) = task {
        val player = lock.read { whitelist.find { it.uuid == uuid } } ?: return@task
        lock.write { player.name = username }
        save()
        updateCache()
    }

    /**
     * Returns a list of players on the specified page.
     *
     * @param page The page number to retrieve.
     * @return A list of players on the specified page.
     */
    fun pageOf(page: Int): List<Player> {
        val pages = lock.read { whitelist.chunked(PageTurner.ITEMS_PER_PAGE) }
        return pages[page - 1]
    }
}
