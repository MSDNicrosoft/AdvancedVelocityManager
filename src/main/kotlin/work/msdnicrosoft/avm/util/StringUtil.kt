package work.msdnicrosoft.avm.util

import com.velocitypowered.api.util.UuidUtils
import net.kyori.adventure.text.Component

object StringUtil {

    /**
     * Repeats a string a specified number of times.
     *
     * @param n The number of times to repeat the string.
     * @return The repeated string.
     */
    operator fun String.times(n: Int) = this.repeat(n)

    /**
     * Replaces occurrences of multiple strings in a string.
     *
     * @param pairs The pairs of strings to replace.
     * @return The modified string.
     */
    fun String.replace(vararg pairs: Pair<String, String>): String {
        var newString = this
        pairs.forEach { (old, new) -> newString = newString.replace(old, new) }
        return newString
    }

    /**
     * Checks if a string is a valid UUID.
     *
     * @return True if the string is a valid UUID, false otherwise.
     */
    fun String.isUuid() = runCatching { this.toUuid() }.isSuccess

    /**
     * Converts a string to a UUID, removing any dashes.
     *
     * @return The UUID representation of the string.
     */
    fun String.toUuid() = UuidUtils.fromUndashed(this.replace("-", ""))

    fun String.formated(): Component = ComponentUtil.serializer.parse(this)!!
}
