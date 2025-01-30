package work.msdnicrosoft.avm.config.data

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable

@Serializable
data class TabSync(
    @YamlComment("Whether to enable tab synchronization")
    var enabled: Boolean = true,

    @YamlComment(
        "The display format for each player in tab list",
        "",
        "Default: &8[%server_nickname%&8] &r%player_name%",
        "",
        "Available placeholders:",
        "%player_name% - The username of a player who sent a message",
        "%server_name% - The name of the server where a player sent a message",
        "%server_nickname% - The nickname of the server where a player sent a message",
    )
    val format: String = "&8[%server_nickname%&8] &r%player_name%"
)
