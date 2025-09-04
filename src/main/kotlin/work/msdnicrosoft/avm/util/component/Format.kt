package work.msdnicrosoft.avm.util.component

import kotlinx.serialization.Serializable
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger

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
) {

    /**
     * Validates the format.
     *
     * @return True if the format is valid, false otherwise.
     */
    fun validate(): Boolean {
        if (this.text.isEmpty()) {
            logger.warn("Invalid format: {}", this)
            logger.warn("Text cannot be empty or blank.")
            return false
        }

        val conflicted: Boolean = listOf(
            !this.command.isNullOrEmpty(),
            !this.suggest.isNullOrEmpty(),
            !this.url.isNullOrEmpty(),
            !this.clipboard.isNullOrEmpty(),
        ).count { it } > 1
        if (conflicted) {
            logger.warn("Invalid format: {}", this)
            logger.warn("Exactly one of 'command', 'suggest', 'url', or 'clipboard' should be provided and non-empty.")
            return false
        }
        return true
    }
}
