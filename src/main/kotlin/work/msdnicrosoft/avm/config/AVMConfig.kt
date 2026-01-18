package work.msdnicrosoft.avm.config

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import work.msdnicrosoft.avm.config.data.*

@Serializable
data class AVMConfig(
    @YamlComment(
        "The version of configuration",
        "",
        "DO NOT CHANGE THIS, OTHERWISE IT MAY CAUSE CRITICAL PROBLEMS"
    )
    val version: Int = 2,

    @YamlComment(
        "The language for plugin to use",
        "The language name must be the part file name of files in the `lang` folder",
        "Example:",
        "   zh_CN.yml -> zh_CN",
        "   en_US.yml -> en_US"
    )
    @SerialName("default-language")
    val defaultLang: String = "en_US",

    @YamlComment(
        "Mapping of the server names (configured in `velocity.toml`) and nicknames",
        "",
        "Format: server-name: server-nickname",
        "Example:",
        "   survival: \"<green>Survival\"",
        "   factions: \"<green>Factions\"",
        "   minigames: \"<yellow>Minigames\""
    )
    @SerialName("server-mapping")
    val serverMapping: Map<String, String> = mapOf(
        "lobby" to "<white>Lobby",
        "factions" to "<green>Factions",
        "minigames" to "<yellow>Minigames"
    ),

    @YamlComment("The event broadcast configuration")
    val broadcast: Broadcast = Broadcast(),

    @YamlComment("The utility commands configuration")
    val utility: Utility = Utility(),

    @YamlComment("The whitelist configuration")
    val whitelist: Whitelist = Whitelist(),

    @YamlComment("The Chat Bridge configuration")
    @SerialName("chat-bridge")
    val chatBridge: ChatBridge = ChatBridge(),

    @YamlComment("The TabList Synchronization configuration")
    @SerialName("tab-sync")
    val tabSync: TabSync = TabSync(),

    @YamlComment("The Map Synchronization configuration")
    @SerialName("map-sync")
    val mapSync: MapSync = MapSync(),

    @YamlComment("The Reconnection configuration")
    val reconnect: Reconnect = Reconnect(),
) {
    /**
     * Retrieves the server nickname from the serverMapping configuration.
     * If no mapping is found for the server, return the original server name.
     *
     * @param server The server name to retrieve the nickname for.
     * @return The server nickname.
     */
    fun getServerNickName(server: String): String = this.serverMapping[server] ?: server
}
