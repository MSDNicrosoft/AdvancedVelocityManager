package work.msdnicrosoft.avm.module.command.session

/**
 * Represents an action to be executed in a command session.
 *
 * @property executed Whether the action has been executed.
 * @property block The block of code to be executed.
 * @property expirationTime The time at which the action expires.
 */
data class Action(var executed: Boolean = false, val block: () -> Any?, val expirationTime: Long) {
    fun isExpired(): Boolean = System.currentTimeMillis() > expirationTime
}
