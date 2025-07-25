package work.msdnicrosoft.avm.util.string

import com.velocitypowered.api.util.UuidUtils
import java.util.*

/**
 * Repeats a string a specified number of times.
 *
 * @param n The number of times to repeat the string.
 * @return The repeated string.
 */
operator fun String.times(n: Int): String = this.repeat(n)

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
@Suppress("NOTHING_TO_INLINE")
inline fun String.isUuid(): Boolean = runCatching { this.toUuid() }.isSuccess

/**
 * Converts a string to a UUID, removing any dashes.
 *
 * @return The UUID representation of the string.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.toUuid(): UUID = UuidUtils.fromUndashed(this.replace("-", ""))

/**
 * Checks if the string is a valid URL.
 *
 * @return True if the current string is a valid URL, false otherwise.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.isValidUrl(): Boolean = StringUtil.URL_PATTERN.matches(this)
