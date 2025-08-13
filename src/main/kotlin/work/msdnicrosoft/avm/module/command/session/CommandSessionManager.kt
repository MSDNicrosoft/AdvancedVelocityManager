package work.msdnicrosoft.avm.module.command.session

import com.google.common.io.BaseEncoding
import com.velocitypowered.api.scheduler.ScheduledTask
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger
import work.msdnicrosoft.avm.util.server.task
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * A manager for command sessions.
 *
 * Command sessions are used to execute commands.
 * This class provides functionality to add, execute, and remove command sessions.
 */
object CommandSessionManager {
    private val sha256 = MessageDigest.getInstance("SHA-256")
    private val base32 = BaseEncoding.base32().omitPadding()
    private val sessions = ConcurrentHashMap<String, Action>()

    /**
     * The task responsible for removing expired command sessions.
     */
    private lateinit var removalTask: ScheduledTask

    /**
     * Initializes the command session manager.
     *
     * This function starts a task that removes expired command sessions every 20 minutes.
     */
    fun init() {
        removalTask = task(repeatInMillis = 20 * 60 * 1000L) {
            sessions.entries.removeIf { it.value.isExpired() }
        }
    }

    /**
     * Disables the command session manager.
     *
     * This function cancels the removal task.
     */
    fun disable() {
        removalTask.cancel()
    }

    fun reload() {
        logger.info("Reloading command sessions...")
        disable()
        sessions.clear()
        init()
    }

    /**
     * Adds a command session to the manager.
     *
     * @param sessionId The ID of the command session.
     * @param block The block of code to be executed.
     */
    fun add(sessionId: String, block: () -> Any?) {
        sessions[sessionId] = Action(block = block, expirationTime = System.currentTimeMillis() + 60_000L)
    }

    /**
     * Executes a command session.
     *
     * @param sessionId The ID of the command session.
     * @return The result of executing the command session.
     */
    fun executeAction(sessionId: String): ExecuteResult {
        val session = sessions.remove(sessionId) ?: return ExecuteResult.NOT_FOUND
        return try {
            if (session.isExpired()) {
                ExecuteResult.EXPIRED
            } else {
                session.block.invoke()
                ExecuteResult.SUCCESS
            }
        } catch (e: Exception) {
            logger.warn("Failed to execute session command", e)
            ExecuteResult.FAILED
        }
    }

    /**
     * Generates a session ID based on the given parameters.
     *
     * @param name The name used to generate the session ID.
     * @param time The time used to generate the session ID.
     * @param command The command used to generate the session ID.
     * @return The generated session ID.
     */
    fun generateSessionId(name: String, time: Long, command: String): String {
        val digest = sha256.digest("$name$time$command".toByteArray())
        return base32.encode(digest)
    }
}
