package work.msdnicrosoft.avm.module.chatbridge

/**
 * Represents the different modes of passthrough for chat messages.
 */
enum class PassthroughMode {
    /**
     * All chat messages will be sent to the backend server.
     */
    ALL,

    /**
     * No chat messages will be sent to the backend server.
     */
    NONE,

    /**
     * If they match one of the patterns,
     * chat messages will be sent to the backend server.
     */
    PATTERN;

    companion object {
        fun of(mode: String): PassthroughMode = valueOf(mode.uppercase())
    }
}
