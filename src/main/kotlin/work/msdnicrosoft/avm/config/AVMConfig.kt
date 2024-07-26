package work.msdnicrosoft.avm.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("MaxLineLength")
@Serializable
data class AVMConfig(
    @SerialName("default-language")
    val defaultLang: String = "en_US",

    @SerialName("server-mapping")
    val serverMapping: Map<String, String> = mapOf(
        "lobby" to "&fLobby",
        "survival" to "&aSurvival",
        "minigames" to "&eMinigames",
        "creative" to "&6Creative",
    ),

    val broadcast: Broadcast = Broadcast(),

    val utility: Utility = Utility(),

    val whitelist: Whitelist = Whitelist(),

    @SerialName("chat-bridge")
    val chatBridge: ChatBridge = ChatBridge()
) {
    @Serializable
    data class Broadcast(
        @SerialName("join")
        var join: Join = Join(),

        @SerialName("leave")
        var leave: Leave = Leave(),

        @SerialName("switch")
        var switch: Switch = Switch(),
    ) {
        @Serializable
        data class Join(
            var enabled: Boolean = true,
            val message: String = "&7[&a+&7]&r %player_name% &7joined server &r%server_nickname%"
        )

        @Serializable
        data class Leave(
            var enabled: Boolean = true,
            val message: String = "&7[&c-&7]&r %player_name% &2left the server"
        )

        @Serializable
        data class Switch(
            var enabled: Boolean = true,
            val message: String = "&8[&b❖&8]&r %player_name% &7:&r %previous_server_nickname% &6➟&r %target_server_nickname%"
        )
    }

    @Serializable
    data class Utility(
        @SerialName("sendall")
        val sendAll: SendAll = SendAll(),

        @SerialName("kickall")
        val kickAll: KickAll = KickAll()
    ) {
        @Serializable
        data class SendAll(
            @SerialName("allow-bypass")
            val allowBypass: Boolean = true
        )

        @Serializable
        data class KickAll(
            @SerialName("allow-bypass")
            val allowBypass: Boolean = true
        )
    }

    @Serializable
    data class Whitelist(
        var enabled: Boolean = false,

        val message: String = "&cYou are not whitelisted on this server.",

        @SerialName("query-api-url")
        val queryApi: QueryApi = QueryApi(),

        @SerialName("cache-players")
        val cachePlayers: CachePlayers = CachePlayers()
    ) {
        @Serializable
        data class QueryApi(
            val uuid: String = "https://api.mojang.com/users/profiles/minecraft/",
            val profile: String = "https://sessionserver.mojang.com/session/minecraft/profile/"
        )

        @Serializable
        data class CachePlayers(
            var enabled: Boolean = false,

            @Serializable
            val maxSize: Int = 20,

            @SerialName("auto-purge")
            val autoPurge: AutoPurge = AutoPurge()
        ) {
            @Serializable
            data class AutoPurge(var enabled: Boolean = false)
        }
    }

    @Serializable
    data class ChatBridge(
        var enabled: Boolean = true,

        @SerialName("allow-format-code")
        val allowFormatCode: Boolean = true,

        @SerialName("chat-format")
        val chatFormat: List<Format> = listOf(
            Format(
                text = "&8[%server_nickname%&8]",
                hover = """
                &7Server %server_name%
                &r
                &7▪ Online: &a%server_online_players%
                &7▪ Version：&6%server_version%
                &r
                &6▶ &eClick to connect to this server
                """.trimIndent(),
                command = "/server %server_name%"
            ),
            Format(
                text = "<&7%player_name%&r>",
                hover = """
                &7▪ Ping: &3%player_ping% ms
                &7▪ UUID: &3%player_uuid%
                """.trimIndent(),
//                &6▶ &e点击向该玩家发送私信
//                &r
//                suggest = "/msg %player_name% " TODO /msg
            ),
            Format(
                text = " &r%player_message%",
                hover = "&7Sent time %player_message_sent_time%"
            )
        ),

//        @SerialName("functions")
//        val functions: Functions = Functions(),

        @SerialName("chat-passthrough")
        val chatPassthrough: ChatPassthrough = ChatPassthrough()
    ) {
        @Serializable
        data class ChatPassthrough(
            var mode: String = "ALL",
            val pattern: Pattern = Pattern()
        ) {

            @Serializable
            data class Pattern(
                val contains: List<String> = listOf("--==GLOBAL-CHAT==--"),

                val startswith: List<String> = listOf("!!", "!localchat"),

                val endswith: List<String> = listOf("--==GLOBAL-CHAT==--")
            )
        }

        @Serializable
        data class Format(
            val text: String,
            val hover: String? = null,
            val command: String? = null,
            val suggest: String? = null,
            val url: String? = null,
            val clipboard: String? = null
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
}
