package work.msdnicrosoft.avm.util.component

import kotlinx.serialization.Serializable

/**
 * A data class representing a format for a component.
 *
 * @property text The text of the component.
 * @property hover The hover text of the component.
 * @property command The command to run when clicked.
 * @property suggest The suggest command to run when clicked.
 * @property url The URL to open when clicked.
 * @property clipboard The text to copy to the clipboard when clicked.
 */
@Serializable
data class Format(
    val text: String,
    val hover: List<String>? = null,
    val command: String? = null,
    val suggest: String? = null,
    val url: String? = null,
    val clipboard: String? = null
)
