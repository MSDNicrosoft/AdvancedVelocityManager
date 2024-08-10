package work.msdnicrosoft.avm.config

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import work.msdnicrosoft.avm.util.component.Format

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
) {
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
            @YamlComment(
                "The join broadcast message",
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
            @YamlComment(
                "The leave broadcast message",

                "Available placeholders:",
                "%player_name% - Username of a player who left the server"
            )
            val message: String = "&7[&c-&7]&r %player_name% &2left the server"
        )

        @Serializable
        data class Switch(
            @YamlComment("Whether to enable switch broadcast")
            var enabled: Boolean = true,
            @YamlComment(
                "The switch broadcast message",
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

    @Serializable
    data class Utility(
        @YamlComment("The `sendall` command configuration")
        @SerialName("sendall")
        val sendAll: SendAll = SendAll(),

        @YamlComment("The `kickall` command configuration")
        @SerialName("kickall")
        val kickAll: KickAll = KickAll()
    ) {
        @Serializable
        data class SendAll(
            @YamlComment("Allow players who have permission avm.kickall.bypass to bypass the send")
            @SerialName("allow-bypass")
            val allowBypass: Boolean = true
        )

        @Serializable
        data class KickAll(
            @YamlComment("Allow players who have permission avm.kickall.bypass to bypass the kick")
            @SerialName("allow-bypass")
            val allowBypass: Boolean = true
        )
    }

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
        val message: String = "&cYou are not whitelisted on this server.",

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
                "Learn more: https://wiki.vg/Mojang_API#Username_to_UUID"
            )
            var uuid: String = "https://api.mojang.com/users/profiles/minecraft/",

            @YamlComment(
                "The API URL to query username by UUID",
                "Learn more: https://wiki.vg/Mojang_API#UUID_to_Profile_and_Skin.2FCape"
            )
            var profile: String = "https://sessionserver.mojang.com/session/minecraft/profile/"
        )

        @Serializable
        data class CachePlayers(
            @YamlComment("Whether to enable cache players")
            var enabled: Boolean = true,

            @YamlComment("The max size of the cache")
            @SerialName("max-size")
            val maxSize: Int = 20
        )
    }

    @Serializable
    data class ChatBridge(
        @YamlComment("Whether to enable Chat Bridge")
        var enabled: Boolean = true,

        @YamlComment(
            "Whether to allow players to use format code in chat",
            "Learn more: ",
            "   Introduction & Basic Usage: https://minecraft.wiki/w/Formatting_codes",
            "   Advanced Usage: https://github.com/Vankka/EnhancedLegacyText/wiki/Format"
        )
        @SerialName("allow-format-code")
        val allowFormatCode: Boolean = true,

        @YamlComment(
            "The public chat format",
            "",
            "Available placeholders:",
            "%player_message% - The message content of a player who sent a message",
            "%player_message_sent_time% - The message sent time",
            "%player_name% - The username of a player who sent a message",
            "%player_uuid% - The UUID of a player who sent a message",
            "%player_ping% - The ping of a player who sent a message",
            "%server_name% - The name of the server where a player sent a message",
            "%server_nickname% - The nickname of the server where a player sent a message",
            "%server_online_players% - The online players of the server where a player sent a message",
            "%server_version% - The version of the server where a player sent a message"
        )
        @SerialName("public-chat-format")
        val publicChatFormat: List<Format> = listOf(
            Format(
                text = "&8[&r%server_nickname%&8]",
                hover = listOf(
                    "&7Server %server_name%",
                    "&r",
                    "&7▪ Online: &a%server_online_players%",
                    "&7▪ Version：&6%server_version%",
                    "&r",
                    "&6▶ &eClick to connect to this server"
                ),
                command = "/server %server_name%"
            ),
            Format(
                text = "<&7%player_name%&r>",
                hover = listOf(
                    "&7▪ Ping: &3%player_ping% ms",
                    "&7▪ UUID: &3%player_uuid%",
                    "&r",
                    "&6▶ &eClick to send private message to this player",
                ),
                suggest = "/msg %player_name% "
            ),
            Format(
                text = " &r%player_message%",
                hover = listOf("&7Sent time: %player_message_sent_time%")
            )
        ),

        @YamlComment(
            "The private chat format",
            "",
            "Available placeholders:",
            "%player_message_sent_time% - The message sent time",
            "%player_message% - The message content of a player who sent a message",
            "%player_name_from% - The username of a player who sent a message",
            "%player_name_to% - The username of a player who receive a message",
        )
        @SerialName("private-chat-format")
        val privateChatFormat: PrivateChatFormat = PrivateChatFormat(),

        @YamlComment(
            "Whether to take over private chat commands: /msg, /w and /tell",
            "This will allow global private chat"
        )
        @SerialName("takeover-private-chat")
        val takeOverPrivateChat: Boolean = true,

//        @SerialName("functions")
//        val functions: Functions = Functions(),

        @YamlComment("The behavior how plugin send chat messages to backend server")
        @SerialName("chat-passthrough")
        val chatPassthrough: ChatPassthrough = ChatPassthrough()
    ) {
        @Serializable
        data class ChatPassthrough(
            @YamlComment(
                "Available modes:",
                "   ALL:",
                "       All chat messages will be sent to backend server",
                "       The custom chat format will not take effect in the server which sender is in",
                "   PATTERN:",
                "       If matching one of the configured pattern(s),",
                "       the chat messages will be sent to backend server",
                "       The matched chat messages will not use custom chat format in the server which sender is in",
                "   NONE:",
                "       No chat messages will be sent to backend server",
                "       If using MCDReforged, Quickshop and etc., please do not use this mode"
            )
            var mode: String = "ALL",

            @YamlComment(
                "This part of configuration is only available",
                "in the `PATTERN` Chat-Passthrough mode"
            )
            val pattern: Pattern = Pattern()
        ) {

            @Serializable
            data class Pattern(
                @YamlComment("Contains any of the item in the following list")
                val contains: List<String> = listOf("--==GLOBAL-CHAT==--"),

                @YamlComment("Starts with any of the item in the following list")
                val startswith: List<String> = listOf("!!", "!localchat"),

                @YamlComment("Ends with any of the item in the following list")
                val endswith: List<String> = listOf("--==GLOBAL-CHAT==--")
            )
        }

        @Serializable
        data class PrivateChatFormat(
            val sender: List<Format> = listOf(
                Format(
                    text = "&8[&7\uD83D\uDD12 &7➦ &7%player_name_to%&8]",
                    hover = listOf("This is a private chat message")
                ),
                Format(
                    text = "<&7%player_name_from%&r>",
                    hover = listOf("&6▶ &eClick to reply privately"),
                    suggest = "/msg %player_name_to% "
                ),
                Format(
                    text = " &r%player_message%",
                    hover = listOf("&7Sent time: %player_message_sent_time%")
                )
            ),
            val receiver: List<Format> = listOf(
                Format(
                    text = "&8[&7\uD83D\uDD12&8]",
                    hover = listOf("This is a private chat message")
                ),
                Format(
                    text = "<&7%player_name_from%&r>",
                    hover = listOf("&6▶ &eClick to reply privately"),
                    suggest = "/msg %player_name_from% "
                ),
                Format(
                    text = " &r%player_message%",
                    hover = listOf("&7Sent time: %player_message_sent_time%")
                )
            )
        )

//        @Serializable
//        data class Functions(
//            val mention: Mention = Mention(),
//
//            @SerialName("process-url")
//            val processUrl: ProcessUrl = ProcessUrl()
//        ) {
//            @Serializable
//            data class Mention(
//                val enabled: Boolean = true,
//                val permission: String = "avm.chat.function.mention",
//
//                @SerialName("allow-self-mention")
//                val allowSelfMention: Boolean = false,
//
//                @SerialName("cooldown-seconds")
//                val cooldownSeconds: Long = 5L,
//
//                @SerialName("mention-all")
//                val mentionAll: MentionAll = MentionAll()
//            ) {
//                @Serializable
//                data class MentionAll(
//                    val enabled: Boolean = true,
//                    val permission: String = "avm.chat.function.mention.all"
//                )
//            }
//
//            @Serializable
//            data class ProcessUrl(
//                val enabled: Boolean = true,
//                val pattern: String = "((https|http|ftp|rtsp|mms)?:\\/\\/)[^\\s]+",
//                val format: Format = Format(
//                    prefix = "&8[",
//                    text = "&f&l网站",
//                    suffix = "&8]",
//                    hover = """
//                    &r
//                    &3网站: %matched_text%
//                    &r
//                    &7点击进入!
//                    &r
//                    &8[&c!&8] &7谨防任何诈骗
//                    """.trimIndent(),
//                    url = "%matched_text%"
//                )
//            )
//
//            @Serializable
//            data class ProcessQQNumber(
//                val enabled: Boolean = true,
//                val pattern: String = "QQ( )?[1-9]([0-9]{5,11})",
//
//                @SerialName("matched-pattern")
//                val matchedPattern: String = "[1-9]([0-9]{5,11})",
//                val format: Format = Format(
//                    prefix = "&8[",
//                    text = "&3&lQQ: %matched_text%",
//                    suffix = "&8]",
//                    hover = """
//
//                    &3QQ: &b%matched_text%
//
//                    &7这是一个 QQ 账号,
//                    &7你可以点击此项快速打开聊天
//
//                    &8[&c!&8] &7请勿进行任何金钱交易
//                    &8[&c!&8] &7交友需谨慎
//                    """.trimIndent(),
//                    url = "https://wpa.qq.com/msgrd?v=3&uin=%matched_text%&site=qq&menu=yes"
//                )
//            )
//
//            @Serializable
//            data class ProcessBilibiliBv(
//
//            )
//        }
    }

    @Serializable
    data class TabSync(
        @YamlComment("Whether to enable tab synchronization")
        var enabled: Boolean = true,

        @YamlComment("The display format for each player in tab list")
        val format: String = "&8[%server_nickname%&8] &r%player_name%"
    )
}
