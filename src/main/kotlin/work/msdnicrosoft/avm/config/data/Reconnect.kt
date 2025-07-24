package work.msdnicrosoft.avm.config.data

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reconnect(
    @YamlComment("Whether to enable the reconnect feature")
    val enabled: Boolean = false,

    @YamlComment("Regex pattern to match server shutdown messages triggering reconnect")
    val pattern: String = "((?i)^(server closed|server is restarting|multiplayer\\.disconnect\\.server_shutdown))+$",

    @YamlComment("Heartbeat interval in milliseconds for reconnect detection")
    @SerialName("ping-interval")
    val pingInterval: Long = 1000L,

    @YamlComment("Heartbeat timeout in milliseconds, considered disconnected if exceeded")
    @SerialName("ping-timeout")
    val pingTimeout: Long = 300L,

    @YamlComment("Interval in milliseconds to refresh reconnect message")
    @SerialName("message-interval")
    val messageInterval: Long = 1000L,

    @YamlComment("Delay in milliseconds before attempting to reconnect")
    @SerialName("reconnect-delay")
    val reconnectDelay: Long = 2000L,

    @YamlComment("Reconnect message configuration")
    val message: Message = Message()
) {
    @Serializable
    data class Message(
        @YamlComment("Message shown while waiting to reconnect")
        val waiting: Waiting = Waiting(),
        @YamlComment("Message shown while connecting")
        val connecting: Connecting = Connecting()
    ) {
        @Serializable
        data class Waiting(
            @YamlComment("Title shown while waiting to reconnect")
            val title: String = "Server Restarting...",
            @YamlComment("Subtitle shown while waiting to reconnect")
            val subtitle: String = "Please wait a moment before reconnecting."
        )

        @Serializable
        data class Connecting(
            @YamlComment("Title shown while connecting")
            val title: String = "Reconnecting...",
            @YamlComment("Subtitle shown while connecting")
            val subtitle: String = "Please wait..."
        )
    }
}
