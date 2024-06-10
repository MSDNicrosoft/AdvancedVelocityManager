package work.msdnicrosoft.avm.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment

@Serializable
data class AVMConfig(
    @Comment("Enable whitelist")
    val enabled: Boolean = false,

    @Comment("Mojang's player query API URL")
    val queryApi: QueryApi
) {
    @SerialName("query-api-url")
    @Serializable
    data class QueryApi(
        val uuid: String = "https://api.mojang.com/users/profiles/minecraft/",
        val profile: String = "https://sessionserver.mojang.com/session/minecraft/profile/"
    )

}
