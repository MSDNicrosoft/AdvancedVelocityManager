package work.msdnicrosoft.avm.module

import com.google.common.io.BaseEncoding
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.service.PlatformExecutor
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.logger
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

typealias ExecuteBlock = () -> Any?
typealias SessionId = String

/**
 * A manager for command sessions.
 *
 * Command sessions are used to execute commands.
 * This class provides functionality to add, execute, and remove command sessions.
 */
@PlatformSide(Platform.VELOCITY)
object CommandSessionManager {

    /**
     * The result of executing a command session.
     */
    enum class ExecuteResult { SUCCESS, EXPIRED, FAILED, NOT_FOUND }

    /**
     * Represents an action to be executed in a command session.
     *
     * @property executed Whether the action has been executed.
     * @property block The block of code to be executed.
     * @property expirationTime The time at which the action expires.
     */
    data class Action(var executed: Boolean = false, val block: ExecuteBlock, val expirationTime: Long) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expirationTime
    }

    private val sessions = ConcurrentHashMap<SessionId, Action>()

    /**
     * The task responsible for removing expired command sessions.
     */
    private lateinit var removalTask: PlatformExecutor.PlatformTask

    /**
     * Initializes the command session manager.
     *
     * This function starts a task that removes expired command sessions every 20 minutes.
     */
    fun init() {
        removalTask = submitAsync(period = 20 * 60L) {
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
    fun add(sessionId: SessionId, block: ExecuteBlock) {
        sessions[sessionId] = Action(block = block, expirationTime = System.currentTimeMillis() + 60_000L)
    }

    /**
     * Executes a command session.
     *
     * @param sessionId The ID of the command session.
     * @return The result of executing the command session.
     */
    fun executeAction(sessionId: SessionId): ExecuteResult {
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
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$name$time$command".toByteArray())
        return BaseEncoding.base32().omitPadding().encode(digest)
    }
}
