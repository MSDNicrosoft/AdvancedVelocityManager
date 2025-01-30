package work.msdnicrosoft.avm.config

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import work.msdnicrosoft.avm.config.data.Broadcast
import work.msdnicrosoft.avm.config.data.ChatBridge
import work.msdnicrosoft.avm.config.data.TabSync
import work.msdnicrosoft.avm.config.data.Utility
import work.msdnicrosoft.avm.config.data.Whitelist

@Serializable
data class AVMConfig(
    @YamlComment(
        "The version of configuration",
        "",
        "DO NOT CHANGE THIS, OTHERWISE IT MAY CAUSE CRITICAL PROBLEMS"
    )
    val version: Int = 1,

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
        "   survival: \"&aSurvival\"",
        "   factions: \"&aFactions\"",
        "   minigames: \"&eMinigames\""
    )
    @SerialName("server-mapping")
    val serverMapping: Map<String, String> = mapOf(
        "lobby" to "&fLobby",
        "factions" to "&aFactions",
        "minigames" to "&eMinigames"
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
    val tabSync: TabSync = TabSync()
)
