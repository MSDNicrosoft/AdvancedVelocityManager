package work.msdnicrosoft.avm.config.data

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Broadcast(
    @YamlComment("When a player joins the server, plugin will send broadcast")
    @SerialName("join")
    val join: Join = Join(),

    @YamlComment("When a player leaves the server, plugin will send broadcast")
    @SerialName("leave")
    val leave: Leave = Leave(),

    @YamlComment("When a player switch from a server to another server, plugin will send broadcast")
    @SerialName("switch")
    val switch: Switch = Switch(),
) {
    @Serializable
    data class Join(
        @YamlComment("Whether to enable join broadcast")
        var enabled: Boolean = true,

        @YamlComment("Whether to enable logging join broadcast to file")
        val logging: Boolean = false,

        @YamlComment(
            "The join broadcast message",
            "",
            "Default: &7[&a+&7]&r %player_name% &7joined server &r%server_nickname%",
            "",
            "Available placeholders:",
            "%player_name% - Username of a player who joined the server",
            "%server_name% - Server name which the player joined in",
            "%server_nickname% - Server nickname which the player joined in"
        )
        val message: String = "&7[&a+&7]&r %player_name% &7joined server &r%server_nickname%"
    )

    @Serializable
    data class Leave(
        @YamlComment("Whether to enable leave broadcast")
        var enabled: Boolean = true,

        @YamlComment("Whether to enable logging leave broadcast to file")
        val logging: Boolean = false,

        @YamlComment(
            "The leave broadcast message",
            "",
            "Default: &7[&c-&7]&r %player_name% &2left the server",
            "",
            "Available placeholders:",
            "%player_name% - Username of a player who left the server"
        )
        val message: String = "&7[&c-&7]&r %player_name% &2left the server"
    )

    @Serializable
    data class Switch(
        @YamlComment("Whether to enable switch broadcast")
        var enabled: Boolean = true,

        @YamlComment("Whether to enable logging switch broadcast to file")
        val logging: Boolean = false,

        @YamlComment(
            "The switch broadcast message",
            "",
            "Default: &8[&b⇄&8]&r %player_name% &7:&r %previous_server_nickname% &6➟&r %target_server_nickname%",
            "",
            "Available placeholders:",
            "%player_name% - Username of a player who joined the server",
            "%previous_server_name% - Server name which the player switched from",
            "%previous_server_nickname% - Server nickname which the player switched from",
            "%target_server_nickname% - Server name which the player switched to",
            "%target_server_name% - Server name which the player switched to"
        )
        val message: String =
            "&8[&b⇄&8]&r %player_name% &7:&r %previous_server_nickname% &6➟&r %target_server_nickname%"
    )
}
