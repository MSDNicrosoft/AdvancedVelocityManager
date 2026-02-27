package work.msdnicrosoft.avm.module.whitelist

import com.velocitypowered.api.scheduler.ScheduledTask
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.eventManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.component.widget.Paginator
import work.msdnicrosoft.avm.util.data.UUIDSerializer
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import work.msdnicrosoft.avm.util.file.writeTextWithBuffer
import work.msdnicrosoft.avm.util.net.http.HttpStatus
import work.msdnicrosoft.avm.util.net.http.YggdrasilApiUtil
import work.msdnicrosoft.avm.util.server.task
import work.msdnicrosoft.avm.util.string.toUuid
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.io.path.div

object WhitelistManager {
    /**
     * A player in the whitelist.
     *
     * @property name The name of the player.
     * @property uuid The UUID of the player.
     * @property onlineMode Whether the player is online mode
     * @property serverList The list of servers the player is allowed to connect to.
     */
    @Serializable
    data class WhitelistEntry(
        var name: String,
        @Serializable(with = UUIDSerializer::class)
        val uuid: UUID,
        var onlineMode: Boolean,
        var serverList: List<String>
    )

    enum class AddResult {
        SUCCESS,
        API_LOOKUP_NOT_FOUND,
        API_LOOKUP_REQUEST_FAILED,
        ALREADY_EXISTS,
        SAVE_FILE_FAILED,
    }

    enum class RemoveResult {
        SUCCESS,
        FAIL_NOT_FOUND,
        SAVE_FILE_FAILED
    }

    private val file: File = (dataDirectory / "whitelist.json").toFile()

    private val whitelist: MutableList<WhitelistEntry> = mutableListOf()

    val usernames: List<String> get() = this.lock.read { this.whitelist.map(WhitelistEntry::name) }
    val uuids: List<UUID> get() = this.lock.read { this.whitelist.map(WhitelistEntry::uuid) }
    val size: Int get() = this.lock.read { this.whitelist.size }
    val isEmpty: Boolean get() = this.lock.read { this.whitelist.isEmpty() }
    val maxPage: Int get() = this.lock.read { Paginator.getMaxPage(this.whitelist.size) }

    private val lock: ReentrantReadWriteLock = ReentrantReadWriteLock()

    fun init(reload: Boolean = false) {
        this.load(reload)
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
     * Adds a player to the whitelist with the specified [uuid], [server], and [onlineMode].
     * @return The result of the addition operation.
     */
    fun add(uuid: UUID, server: String, onlineMode: Boolean? = null): AddResult {
        val player: WhitelistEntry? = this.getPlayer(uuid)
        return if (player != null) {
            this.add(player.name, uuid, server, onlineMode)
        } else {
            when (val username: String? = YggdrasilApiUtil.getUsername(uuid)) {
                null -> AddResult.API_LOOKUP_REQUEST_FAILED
                HttpStatus.NOT_FOUND.description -> AddResult.API_LOOKUP_NOT_FOUND
                else -> this.add(username, uuid, server, onlineMode)
            }
        }
    }

    /**
     * Adds a player to the whitelist with the specified [username], [server], and [onlineMode].
     *
     * @return The result of the addition operation.
     */
    fun add(username: String, server: String, onlineMode: Boolean? = null): AddResult {
        val player: WhitelistEntry? = this.getPlayer(username)
        return if (player != null) {
            this.add(username, player.uuid, server, onlineMode)
        } else {
            when (val uuid: String? = YggdrasilApiUtil.getUuid(username, onlineMode)) {
                null -> AddResult.API_LOOKUP_REQUEST_FAILED
                HttpStatus.NOT_FOUND.description -> AddResult.API_LOOKUP_NOT_FOUND
                else -> this.add(username, uuid.toUuid(), server, onlineMode)
            }
        }
    }

    /**
     * Adds a player to the whitelist with the specified [username], [uuid], [server], and [onlineMode].
     *
     * @return The result of the addition operation.
     */
    fun add(username: String, uuid: UUID, server: String, onlineMode: Boolean?): AddResult {
        this.lock.write {
            val player: WhitelistEntry? = this.whitelist.find { it.uuid == uuid }
            if (player == null) {
                this.whitelist.add(
                    WhitelistEntry(
                        username,
                        uuid,
                        onlineMode ?: YggdrasilApiUtil.serverIsOnlineMode,
                        mutableListOf(server)
                    )
                )
            } else {
                val noOnlineModeChange: Boolean = onlineMode == null || onlineMode == player.onlineMode
                if (server in player.serverList && username == player.name && noOnlineModeChange) {
                    return AddResult.ALREADY_EXISTS
                }
                if (server !in player.serverList) {
                    player.serverList += server
                }
                if (username != player.name) {
                    player.name = username
                }
                if (onlineMode != null) {
                    player.onlineMode = onlineMode
                }
            }
        }
        return if (this.save(false)) AddResult.SUCCESS else AddResult.SAVE_FILE_FAILED
    }

    /**
     * Removes a player [username] from the whitelist
     * for a specific [server] (If null, the player will be removed from the global whitelist).
     *
     * @return The result of the remove operation.
     */
    fun remove(username: String, server: String?): RemoveResult {
        val mutated = this.lock.write {
            val player: WhitelistEntry = this.whitelist.find { it.name == username }
                ?: return RemoveResult.FAIL_NOT_FOUND
            this.removeFromList(player, server)
        }
        if (!mutated) return RemoveResult.FAIL_NOT_FOUND
        return this.saveResult()
    }

    /**
     * Removes a player [uuid] from the whitelist
     * for a specific [server] (If null, the player will be removed from the global whitelist).
     *
     * @return The result of the remove operation.
     */
    fun remove(uuid: UUID, server: String?): RemoveResult {
        val mutated = this.lock.write {
            val player: WhitelistEntry = this.whitelist.find { it.uuid == uuid } ?: return RemoveResult.FAIL_NOT_FOUND
            this.removeFromList(player, server)
        }
        if (!mutated) return RemoveResult.FAIL_NOT_FOUND
        return this.saveResult()
    }

    /**
     * Removes a [player] from a specific [server], or from the global whitelist if [server] is null.
     * Must be called within a write lock.
     *
     * @return `true` if the whitelist was modified, `false` if the server was not found.
     */
    private fun removeFromList(player: WhitelistEntry, server: String?): Boolean {
        if (server != null) {
            if (server !in player.serverList) return false
            player.serverList -= server
            if (player.serverList.isNotEmpty()) return true
        }
        this.whitelist.remove(player)
        return true
    }

    private fun saveResult(): RemoveResult = if (this.save(false)) {
        RemoveResult.SUCCESS
    } else {
        RemoveResult.SAVE_FILE_FAILED
    }

    /**
     * Clears the whitelist by removing all players from it.
     *
     * @return `true` if the whitelist was successfully cleared and saved, `false` otherwise.
     */
    fun clear(): Boolean {
        this.lock.write {
            this.whitelist.clear()
        }
        return this.save(false)
    }

    /**
     * Finds players in the whitelist by [keyword] and returns them in specified [page].
     */
    fun find(keyword: String, page: Int): List<WhitelistEntry> {
        val filtered = this.lock.read { this.whitelist.filter { keyword in it.name } }
        val offset = (page - 1) * Paginator.ITEMS_PER_PAGE
        return filtered.drop(offset).take(Paginator.ITEMS_PER_PAGE)
    }

    /**
     * Finds a player in the whitelist by their [username].
     */
    fun getPlayer(username: String): WhitelistEntry? = this.lock.read { this.whitelist.find { it.name == username } }

    /**
     * Finds a player in the whitelist by their [uuid].
     */
    fun getPlayer(uuid: UUID): WhitelistEntry? = this.lock.read { this.whitelist.find { it.uuid == uuid } }

    /**
     * Checks if a player with the given [uuid] is allowed to connect to a specific [server].
     */
    fun isListed(uuid: UUID, server: String? = null): Boolean = lock.read {
        // Check if the player is in the whitelist
        val player: WhitelistEntry = this.whitelist.find { it.uuid == uuid } ?: return false

        if (server == null) {
            return true
        }

        // Check if the player is in the server whitelist
        val serverList: List<String> = player.serverList
        if (server in serverList) {
            return true
        }

        return ConfigManager.config.whitelist.serverGroups.any { (group: String, servers: List<String>) ->
            group in serverList && server in servers
        }
    }

    fun updatePlayer(username: String, uuid: UUID): ScheduledTask = task {
        this.lock.write {
            val player: WhitelistEntry = this.whitelist.find { it.uuid == uuid } ?: return@task
            player.name = username
        }
        this.save(false)
    }

    /**
     * Returns a list of whitelist players on the specified [page].
     */
    fun pageOf(page: Int): List<WhitelistEntry> {
        val offset = (page - 1) * Paginator.ITEMS_PER_PAGE
        return this.lock.read { this.whitelist.drop(offset).take(Paginator.ITEMS_PER_PAGE) }
    }

    /**
     * Saves the whitelist to disk.
     *
     * @return True if the save was successful, false otherwise.
     */
    private fun save(initialize: Boolean): Boolean {
        if (!this.file.exists() && initialize) {
            logger.info("Whitelist file does not exist, creating...")
        }

        return try {
            this.file.parentFile.mkdirs()
            this.lock.read {
                val content: String = if (initialize) "[]" else JSON.encodeToString(this.whitelist)
                this.file.writeTextWithBuffer(content)
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
        if (!this.file.exists()) {
            return this.save(initialize = true)
        }

        logger.info("{} whitelist...", if (reload) "Reloading" else "Loading")

        return try {
            this.lock.write {
                this.whitelist.clear()
                this.whitelist.addAll(JSON.decodeFromString<List<WhitelistEntry>>(this.file.readTextWithBuffer()))
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
}
