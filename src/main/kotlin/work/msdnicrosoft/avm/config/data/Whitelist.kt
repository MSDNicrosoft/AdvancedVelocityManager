package work.msdnicrosoft.avm.config.data

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Whitelist(
    @YamlComment("Whether to enable whitelist")
    var enabled: Boolean = false,

    @YamlComment("The server groups to add/remove whitelist bulky")
    @SerialName("server-groups")
    var serverGroups: Map<String, List<String>> = mapOf(
        "Default" to listOf("lobby"),
        "Games" to listOf("factions", "minigames")
    ),

    @YamlComment("The message sent to a not whitelisted player")
    val message: String = "<red>You are not whitelisted on this server.",

    @YamlComment(
        "The API URLs to query for whitelist",
        "",
        "DO NOT MODIFY THIS PART OF CONFIGURATION",
        "IF YOU DO NOT KNOW WHAT YOU ARE DOING!!!",
    )
    @SerialName("query-api-url")
    val queryApi: QueryApi = QueryApi(),

    @YamlComment(
        "Cache not-whitelisted players who attempted to join server",
        "This provides extra Username completion source for command `/avmwl add`"
    )
    @SerialName("cache-players")
    val cachePlayers: CachePlayers = CachePlayers()
) {
    @Serializable
    data class QueryApi(
        @YamlComment(
            "The API URL to query UUID by username",
            "",
            "Default: https://api.minecraftservices.com/minecraft/profile/lookup/name/",
            "",
            "Learn more: https://minecraft.wiki/w/Mojang_API#Query_player's_UUID"
        )
        var uuid: String = "https://api.minecraftservices.com/minecraft/profile/lookup/name/",

        @YamlComment(
            "The API URL to query username by UUID",
            "",
            "Default: https://api.minecraftservices.com/minecraft/profile/lookup/",
            "",
            "Learn more: https://minecraft.wiki/w/Mojang_API#Query_player's_username"
        )
        var profile: String = "https://api.minecraftservices.com/minecraft/profile/lookup/",
    )

    @Serializable
    data class CachePlayers(
        @YamlComment("Whether to enable cache players")
        var enabled: Boolean = true,

        @YamlComment(
            "The max size of the cache",
            "",
            "Default: 20"
        )
        @SerialName("max-size")
        var maxSize: Int = 20
    )

    /**
     * Checks if a server with the given name belongs to a server group.
     *
     * @param name The name of the server to check.
     * @return True if the server belongs to a server group, false otherwise.
     */
    fun isServerGroup(name: String): Boolean = name in this.serverGroups.keys

    /**
     * Retrieves a list of servers that belong to the specified group.
     *
     * @param groupName The name of the server group to retrieve servers for.
     */
    fun getServersInGroup(groupName: String): List<String> = this.serverGroups[groupName].orEmpty()
}
