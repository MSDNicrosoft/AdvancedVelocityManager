package work.msdnicrosoft.avm.config.data

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
