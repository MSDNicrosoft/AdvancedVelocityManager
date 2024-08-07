package work.msdnicrosoft.avm.util.component

import kotlinx.serialization.Serializable

@Serializable
data class Format(
    val text: String,
    val hover: List<String>? = null,
    val command: String? = null,
    val suggest: String? = null,
    val url: String? = null,
    val clipboard: String? = null
)
