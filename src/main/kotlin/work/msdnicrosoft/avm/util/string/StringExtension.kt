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
 * Checks if a string is a valid UUID.
 *
 * @return True if the string is a valid UUID, false otherwise.
 */
fun String.isUuid(): Boolean = runCatching { this.toUuid() }.isSuccess

/**
 * Converts a string to a UUID, removing any dashes.
 *
 * @return The UUID representation of the string.
 */
fun String.toUuid(): UUID = UuidUtils.fromUndashed(this.replace("-", ""))

/**
 * Checks if the string is a valid URL.
 *
 * @return True if the current string is a valid URL, false otherwise.
 */
fun String.isValidUrl(): Boolean = StringUtil.URL_PATTERN.matches(this)
