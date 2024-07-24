package work.msdnicrosoft.avm.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
            val message: String = "&7[&a+&7]&r {player} &7joined server &r{server_nickname}"
        )

        @Serializable
        data class Leave(
            var enabled: Boolean = true,
            val message: String = "&7[&c-&7]&r {player} &2left the server"
        )

        @Serializable
        data class Switch(
            var enabled: Boolean = true,
            val message: String = "&8[&b❖&8]&r {player} &7:&r {previous_server_nickname} &6➟&r {target_server_nickname}"
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
                &7服务器 %server_name%
                &r
                &7▪ 在线: &a%server_online_players%
                &7▪ 版本：&6%server_version%
                &r
                &6▶ &e点击连接至此服务器
                """.trimIndent(),
                command = "/server %server_name%"
            ),
            Format(
                text = "<&7%player_name%&r>",
                hover = """
                &8▪ &7延迟: &3%player_ping% ms
                &8▪ &6UUID: &3%player_uuid%
                &r
                &6▶ &e点击向该玩家发送私信
                &r
                """.trimIndent(),
                suggest = "/msg %player_name% "
            ),
            Format(
                text = " &r%player_message%",
                hover = "&7发送时间 %player_message_sent_time%"
            )
        ),

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
                val contains: List<String> = listOf("--GLOBAL-CHAT--"),

                val startswith: List<String> = listOf("!!", "!globalchat"),

                val endswith: List<String> = listOf("--GLOBAL-CHAT--")
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
    }
}
