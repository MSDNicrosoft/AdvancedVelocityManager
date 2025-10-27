package work.msdnicrosoft.avm.util.component

import kotlinx.serialization.Serializable

/**
 * A format for a component.
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
    init {
        require(this.text.isNotBlank()) { "Text cannot be empty or blank." }

        val click: List<String> = listOfNotNull(this.command, this.suggest, this.url, this.clipboard)

        require(click.size <= 1) { "Cannot specify multiple actions for a format." }
        require(click.all { it.isNotBlank() }) { "Cannot specify empty or blank actions for a format." }
    }

    fun applyReplace(replacer: String.() -> String): Format = Format(
        this.text.replacer(),
        this.hover?.map { it.replacer() },
        this.command?.replacer(),
        this.suggest?.replacer(),
        this.url?.replacer(),
        this.clipboard?.replacer()
    )
}
