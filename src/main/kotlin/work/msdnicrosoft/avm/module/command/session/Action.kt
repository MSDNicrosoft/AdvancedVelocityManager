package work.msdnicrosoft.avm.module.command.session

/**
 * An action [block] to be executed in a command session.
 *
 * The action expires after a specified [expirationTime].
 */
data class Action<T>(private val block: () -> T, val expirationTime: Long) {
    fun isExpired(): Boolean = System.currentTimeMillis() > this.expirationTime

    fun execute(): T = block.invoke()
}
