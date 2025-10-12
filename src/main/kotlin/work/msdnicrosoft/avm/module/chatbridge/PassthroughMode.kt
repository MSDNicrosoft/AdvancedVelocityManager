package work.msdnicrosoft.avm.module.chatbridge

/**
 * The modes of passthrough for chat messages.
 */
enum class PassthroughMode {
    /**
     * All chat messages will be sent to the backend servers.
     */
    ALL,

    /**
     * No chat messages will be sent to the backend servers.
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
