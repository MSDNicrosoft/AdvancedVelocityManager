package work.msdnicrosoft.avw.config

import com.jillesvangurp.jsondsl.JsonDsl
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder

class AVWConfig: JsonDsl(
) {
    val enabled by property(
        customPropertyName = "enabled",
        defaultValue = false
    )
    val authApiUrl by property(
        customPropertyName = "auth-api-url",
        defaultValue = AuthApi(
            uuid = "https://api.mojang.com/users/profiles/minecraft/",
            profile = "https://sessionserver.mojang.com/session/minecraft/profile/"
        )
    )

    data class AuthApi(
        val uuid: String,
        val profile: String
    )

}

