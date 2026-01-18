package work.msdnicrosoft.avm.config.data

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import work.msdnicrosoft.avm.util.component.Format

@Serializable
data class ChatBridge(
    @YamlComment("Whether to enable Chat Bridge")
    var enabled: Boolean = true,

    @YamlComment("Whether to enable logging chat messages to file")
    val logging: Boolean = false,

    @YamlComment(
        "Whether to allow players to use format code in chat",
        "Learn more: ",
        "   Introduction & Usage: https://docs.advntr.dev/minimessage/index.html",
        "   Viewer: https://webui.advntr.dev/"
    )
    @SerialName("allow-format-code")
    val allowFormatCode: Boolean = true,

    @YamlComment(
        "The public chat format",
        "",
        "Available placeholders:",
        "<player_message> - The message content of a player who sent a message",
        "<player_message_sent_time> - The message sent time",
        "<player_name> - The username of a player who sent a message",
        "<player_uuid> - The UUID of a player who sent a message",
        "<player_ping> - The ping of a player who sent a message",
        "<server_name> - The name of the server where a player sent a message",
        "<server_nickname> - The nickname of the server where a player sent a message",
        "<server_online_players> - The online players of the server where a player sent a message",
        "<server_version> - The version of the server where a player sent a message"
    )
    @SerialName("public-chat-format")
    val publicChatFormat: List<Format> = listOf(
        Format(
            text = "<dark_gray>[<reset><server_nickname><dark_gray>]",
            hover = listOf(
                "<gray>Server <server_name>",
                "<reset>",
                "<gray>▪ Online: <green><server_online_players>",
                "<gray>▪ Version：<gold><server_version>",
                "<reset>",
                "<gold>▶ <yellow>Click to connect to this server"
            ),
            command = "/server <server_name>"
        ),
        Format(
            text = "<<gray><player_name><reset>>",
            hover = listOf(
                "<gray>▪ Ping: <dark_aqua><player_ping> ms",
                "<gray>▪ UUID: <dark_aqua><player_uuid>",
                "<reset>",
                "<gold>▶ <yellow>Click to send private message to this player",
            ),
            suggest = "/msg <player_name> "
        ),
        Format(
            text = "<reset> <player_message>",
            hover = listOf("<gray>Sent time: <player_message_sent_time>")
        )
    ),

    @YamlComment(
        "The private chat format",
        "",
        "Available placeholders:",
        "<player_message_sent_time> - The message sent time",
        "<player_message> - The message content of a player who sent a message",
        "<player_name_from> - The username of a player who sent a message",
        "<player_name_to> - The username of a player who receive a message",
    )
    @SerialName("private-chat-format")
    val privateChatFormat: PrivateChatFormat = PrivateChatFormat(),

    @YamlComment(
        "Whether to take over private chat commands: /msg, /w and /tell",
        "This will allow global private chat"
    )
    @SerialName("takeover-private-chat")
    val takeOverPrivateChat: Boolean = true,

//        val functions: Functions = Functions(),

    @YamlComment("The behavior how plugin send chat messages to backend server")
    @SerialName("chat-passthrough")
    val chatPassthrough: ChatPassthrough = ChatPassthrough()
) {
    @Serializable
    data class ChatPassthrough(
        @YamlComment(
            "Default: ALL",
            "",
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
            val contains: List<String> = listOf(),

            @YamlComment("Starts with any of the item in the following list")
            val startswith: List<String> = listOf("!!", "==", "="),

            @YamlComment("Ends with any of the item in the following list")
            val endswith: List<String> = listOf()
        )
    }

    @Serializable
    data class PrivateChatFormat(
        val sender: List<Format> = listOf(
            Format(
                text = "<dark_gray>[<gray>\uD83D\uDD12 <gray>➦ <gray><player_name_to><dark_gray>]",
                hover = listOf("This is a private chat message")
            ),
            Format(
                text = "<<gray><player_name_from><reset>>",
                hover = listOf("<gold>▶ <yellow>Click to reply privately"),
                suggest = "/msg <player_name_to> "
            ),
            Format(
                text = " <reset><player_message>",
                hover = listOf("<gray>Sent time: <player_message_sent_time>")
            )
        ),
        val receiver: List<Format> = listOf(
            Format(
                text = "<dark_gray>[<gray>\uD83D\uDD12<dark_gray>]",
                hover = listOf("This is a private chat message")
            ),
            Format(
                text = "<<gray><player_name_from><reset>>",
                hover = listOf("<gold>▶ <yellow>Click to reply privately"),
                suggest = "/msg <player_name_from> "
            ),
            Format(
                text = " <reset><player_message>",
                hover = listOf("<gray>Sent time: <player_message_sent_time>")
            )
        )
    )

//        @Serializable
//        data class Functions(
//            @SerialName("builtin")
//            val builtin: List<Function> = listOf(),
//            @SerialName("custom")
//            val custom: List<Function> = listOf()
//        ) {
//            @Serializable
//            data class Function(
//                val enabled: Boolean,
//                val priority: Int,
//                val permission: String? = null,
//
//            )
//        }

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
//                    prefix = "<dark_gray>[",
//                    text = "<white><bold>网站",
//                    suffix = "<dark_gray>]",
//                    hover = """
//                    <reset>
//                    <dark_aqua>网站: >matched_text>
//                    <reset>
//                    <gray>点击进入!
//                    <reset>
//                    <dark_gray>[<red>!<dark_gray>] <gray>谨防任何诈骗
//                    """.trimIndent(),
//                    url = ">matched_text>"
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
//                    prefix = "<dark_gray>[",
//                    text = "<dark_aqua><bold>QQ: >matched_text>",
//                    suffix = "<dark_gray>]",
//                    hover = """
//
//                    <dark_aqua>QQ: <aqua>>matched_text>
//
//                    <gray>这是一个 QQ 账号,
//                    <gray>你可以点击此项快速打开聊天
//
//                    <dark_gray>[<red>!<dark_gray>] <gray>请勿进行任何金钱交易
//                    <dark_gray>[<red>!<dark_gray>] <gray>交友需谨慎
//                    """.trimIndent(),
//                    url = "https://wpa.qq.com/msgrd?v=3&uin=>matched_text>&site=qq&menu=yes"
//                )
//            )
//
//            @Serializable
//            data class ProcessBilibiliBv(
//
//            )
//        }
}
