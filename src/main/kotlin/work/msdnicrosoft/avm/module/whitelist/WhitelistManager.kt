package work.msdnicrosoft.avm.module.whitelist

import com.velocitypowered.api.util.UuidUtils
import kotlinx.serialization.SerializationException
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.eventManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.module.whitelist.data.ApiResponse
import work.msdnicrosoft.avm.module.whitelist.data.Player
import work.msdnicrosoft.avm.module.whitelist.result.AddResult
import work.msdnicrosoft.avm.module.whitelist.result.RemoveResult
import work.msdnicrosoft.avm.util.command.PageTurner
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import work.msdnicrosoft.avm.util.file.writeTextWithBuffer
import work.msdnicrosoft.avm.util.net.http.HttpStatus
import work.msdnicrosoft.avm.util.net.http.HttpUtil
import work.msdnicrosoft.avm.util.server.task
import work.msdnicrosoft.avm.util.string.toUuid
import java.io.File
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
    val usernames: HashSet<String> = hashSetOf()
    val uuids: HashSet<UUID> = hashSetOf()

    val size: Int get() = this.whitelist.size

    val isEmpty: Boolean get() = this.whitelist.isEmpty()

    val maxPage: Int get() = PageTurner.getMaxPage(this.whitelist.size)

    inline val serverIsOnlineMode: Boolean get() = server.configuration.isOnlineMode

    private inline val config get() = ConfigManager.config.whitelist

    private val FILE: File = (dataDirectory / "whitelist.json").toFile()

    private val lock = ReentrantReadWriteLock()

    private val httpClient: HttpClient = HttpClient.newHttpClient()

    private val whitelist: MutableList<Player> = mutableListOf()

    var enabled: Boolean
        get() = config.enabled
        set(value) {
            config.enabled = value
            ConfigManager.save()
        }

    /**
     * Called when the plugin is enabled.
     *
     * @param reload If true, the whitelist will be reloaded from the disk.
     */
    fun init(reload: Boolean = false) {
        this.load(reload)
        this.updateCache()
        eventManager.register(plugin, WhitelistHandler)
    }

    fun disable(): Boolean {
        eventManager.unregisterListener(plugin, WhitelistHandler)
        return this.save(false)
    }

    fun reload() {
        eventManager.unregisterListener(plugin, WhitelistHandler)
        this.init(reload = true)
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
        val player: Player? = this.getPlayer(uuid)
        return if (player != null) {
            this.add(player.name, uuid, server, onlineMode)
        } else {
            when (val username: String? = this.getUsername(uuid)) {
                null -> AddResult.API_LOOKUP_REQUEST_FAILED
                HttpStatus.NOT_FOUND.description -> AddResult.API_LOOKUP_NOT_FOUND
                else -> this.add(username, uuid, server, onlineMode)
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
        val player: Player? = this.getPlayer(username)
        return if (player != null) {
            this.add(username, player.uuid, server, onlineMode)
        } else {
            when (val uuid: String? = this.getUuid(username, onlineMode)) {
                null -> AddResult.API_LOOKUP_REQUEST_FAILED
                HttpStatus.NOT_FOUND.description -> AddResult.API_LOOKUP_NOT_FOUND
                else -> this.add(username, uuid.toUuid(), server, onlineMode)
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
        val player: Player? = this.getPlayer(uuid)
        this.lock.write {
            if (player == null) {
                this.whitelist.add(Player(username, uuid, onlineMode ?: this.serverIsOnlineMode, mutableListOf(server)))
                this.uuids.add(uuid)
                this.usernames.add(username)
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
        return if (this.save(false)) AddResult.SUCCESS else AddResult.SAVE_FILE_FAILED
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
        val player: Player = this.getPlayer(username) ?: return RemoveResult.FAIL_NOT_FOUND
        return this.remove(player, server)
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
        val player: Player = this.getPlayer(uuid) ?: return RemoveResult.FAIL_NOT_FOUND
        return this.remove(player, server)
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
        this.lock.write {
            if (server != null) {
                if (server !in player.serverList) return RemoveResult.FAIL_NOT_FOUND

                // Remove the server from the player's server list
                player.serverList -= server

                // If the server list is now empty, remove the player from the global whitelist
                if (player.serverList.isEmpty()) this.whitelist.remove(player)
            } else {
                // Remove the player from the global whitelist
                this.whitelist.remove(player)
            }
            this.uuids.remove(player.uuid)
            this.usernames.remove(player.name)
        }
        return if (this.save(false)) RemoveResult.SUCCESS else RemoveResult.SAVE_FILE_FAILED
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
        this.lock.write {
            this.whitelist.clear()
            this.uuids.clear()
            this.usernames.clear()
        }
        return this.save(false)
    }

    /**
     * Finds players in the whitelist by their username and returns them in pages.
     *
     * @param keyword The keyword to search for.
     * @param page The page number to return.
     * @return A list of players matching the search criteria.
     */
    fun find(keyword: String, page: Int): List<Player> =
        this.lock.read { this.whitelist.filter { keyword in it.name } }
            .chunked(PageTurner.ITEMS_PER_PAGE)
            .getOrNull(page - 1)
            .orEmpty()

    /**
     * Finds a player in the whitelist by their UUID.
     */
    fun getPlayer(username: String): Player? = this.lock.read { this.whitelist.find { it.name == username } }

    /**
     * Finds a player in the whitelist by their UUID.
     */
    fun getPlayer(uuid: UUID): Player? = this.lock.read { this.whitelist.find { it.uuid == uuid } }

    /**
     * Checks if a player with the given UUID is allowed to connect to a specific server.
     *
     * This function first checks if the player is in the whitelist. If not, it immediately returns false.
     * If the player is in the whitelist, it then checks if the server is in the list of allowed servers for the player.
     */
    fun isInServerWhitelist(uuid: UUID, server: String): Boolean = lock.read {
        val player: Player = this.whitelist.find { it.uuid == uuid } ?: return false

        val serverList: List<String> = player.serverList
        if (server in serverList) return true

        return config.serverGroups.any { (group: String, servers: List<String>) ->
            group in serverList && server in servers
        }
    }

    fun updatePlayer(username: String, uuid: UUID) = task {
        val player: Player = this.lock.read { this.whitelist.find { it.uuid == uuid } } ?: return@task
        this.lock.write { player.name = username }
        this.save(false)
        this.updateCache()
    }

    /**
     * Returns a list of players on the specified page.
     *
     * @param page The page number to retrieve.
     * @return A list of players on the specified page.
     */
    fun pageOf(page: Int): List<Player> {
        val pages: List<List<Player>> = this.lock.read { this.whitelist.chunked(PageTurner.ITEMS_PER_PAGE) }
        return pages[page - 1]
    }

    /**
     * Saves the whitelist to disk.
     *
     * @return True if the save was successful, false otherwise.
     */
    private fun save(initialize: Boolean): Boolean {
        if (!this.FILE.exists()) {
            logger.info("Whitelist file does not exist{}", if (initialize) ", creating..." else "")
        }

        return try {
            this.FILE.parentFile.mkdirs()
            this.lock.read {
                this.FILE.writeTextWithBuffer(
                    JSON.encodeToString(if (initialize) listOf() else this.whitelist)
                )
            }
            true
        } catch (e: IOException) {
            logger.error("Failed to save whitelist", e)
            false
        }
    }

    /**
     * Loads the whitelist from the disk.
     *
     * @param reload If true, the whitelist will be reloaded from the disk.
     */
    private fun load(reload: Boolean = false): Boolean {
        if (!this.FILE.exists()) return save(initialize = true)

        logger.info("{} whitelist...", if (reload) "Reloading" else "Loading")

        return try {
            this.lock.write {
                this.whitelist.clear()
                this.whitelist.addAll(JSON.decodeFromString<List<Player>>(this.FILE.readTextWithBuffer()))
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
        this.lock.write {
            this.uuids.clear()
            this.uuids.addAll(this.whitelist.map { it.uuid })

            this.usernames.clear()
            this.usernames.addAll(this.whitelist.map { it.name })
        }
    }

    /**
     * Retrieves the username associated with the given UUID.
     * If the server is in offline mode, it returns null.
     * If the server is online, a query is made to the API to retrieve the username.
     *
     * @param uuid The UUID of the player.
     * @return The username associated with the UUID or null if the query fails.
     */
    private fun getUsername(uuid: UUID): String? {
        if (!this.serverIsOnlineMode) return null

        val request: HttpRequest = HttpRequest.newBuilder()
            .setHeader("User-Agent", HttpUtil.USER_AGENT)
            .uri(URI.create("${config.queryApi.profile.trimEnd('/')}/${UuidUtils.toUndashed(uuid)}"))
            .build()

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { response: HttpResponse<String> ->
                when (val status: HttpStatus = HttpStatus.fromValue(response.statusCode())) {
                    HttpStatus.OK -> JSON.decodeFromString<ApiResponse>(response.body()).name
                    HttpStatus.NOT_FOUND, HttpStatus.NO_CONTENT -> HttpStatus.NOT_FOUND.description
                    HttpStatus.TOO_MANY_REQUESTS -> {
                        logger.warn("Exceeded to the rate limit of Profile API, please retry UUID {}", uuid)
                        null
                    }

                    else -> {
                        logger.warn("Failed to query UUID {}, status code: {}", uuid, status)
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
     * @return The UUID associated with the username or null if the query fails.
     */
    private fun getUuid(username: String, onlineMode: Boolean? = null): String? {
        if (onlineMode == false && !this.serverIsOnlineMode) {
            return UuidUtils.toUndashed(UuidUtils.generateOfflinePlayerUuid(username))
        }

        val request: HttpRequest = HttpRequest.newBuilder()
            .setHeader("User-Agent", HttpUtil.USER_AGENT)
            .uri(URI.create("${config.queryApi.uuid.trimEnd('/')}/$username"))
            .build()

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { response: HttpResponse<String> ->
                when (val status: HttpStatus = HttpStatus.fromValue(response.statusCode())) {
                    HttpStatus.OK -> JSON.decodeFromString<ApiResponse>(response.body()).id
                    HttpStatus.NOT_FOUND -> HttpStatus.NOT_FOUND.description
                    HttpStatus.TOO_MANY_REQUESTS -> {
                        logger.warn("Exceeded to the rate limit of UUID API, please retry username {}", username)
                        null
                    }

                    else -> {
                        logger.warn("Failed to query username {}, status code: {}", username, status)
                        null
                    }
                }
            }.get()
    }
}
