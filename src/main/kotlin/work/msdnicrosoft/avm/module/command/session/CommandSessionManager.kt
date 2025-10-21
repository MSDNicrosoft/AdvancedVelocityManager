package work.msdnicrosoft.avm.module.command.session

import com.google.common.io.BaseEncoding
import com.velocitypowered.api.scheduler.ScheduledTask
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger
import work.msdnicrosoft.avm.util.server.task
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes

object CommandSessionManager {
    private val SHA256: MessageDigest = MessageDigest.getInstance("SHA-256")
    private val BASE32: BaseEncoding = BaseEncoding.base32().omitPadding()

    private val sessions: ConcurrentHashMap<String, Action<*>> = ConcurrentHashMap()

    /**
     * The task responsible for removing expired command sessions.
     */
    private lateinit var removalTask: ScheduledTask

    fun init() {
        // Every 20 minutes, remove all expired sessions
        this.removalTask = task(repeat = 20L.minutes) {
            this.sessions.entries.removeIf { it.value.isExpired() }
        }
    }

    fun disable() {
        this.removalTask.cancel()
    }

    fun reload() {
        logger.info("Reloading command sessions...")
        this.disable()
        this.sessions.clear()
        this.init()
    }

    /**
     * Adds a command session with [sessionId] and [block] to be executed to the manager.
     */
    fun <T> add(sessionId: String, block: () -> T) {
        sessions[sessionId] = Action(block = block, expirationTime = System.currentTimeMillis() + 60_000L)
    }

    /**
     * Executes a specified [sessionId] command session.
     *
     * @return The result of executing the command session.
     */
    fun executeAction(sessionId: String): ExecuteResult {
        val action = sessions.remove(sessionId) ?: return ExecuteResult.NOT_FOUND
        return try {
            if (action.isExpired()) {
                ExecuteResult.EXPIRED
            } else {
                action.execute()
                ExecuteResult.SUCCESS
            }
        } catch (e: Exception) {
            logger.warn("Failed to execute session command", e)
            ExecuteResult.FAILED
        }
    }

    /**
     * Generates a session ID based on the provided [name], [time], and [command].
     */
    fun generateSessionId(name: String, time: Long, command: String): String {
        val digest: ByteArray = SHA256.digest("$name$time$command".toByteArray())
        return BASE32.encode(digest)
    }
}
