package work.msdnicrosoft.avm.util.string

import com.velocitypowered.api.util.UuidUtils
import java.util.*

/**
 * Checks if a string is a valid UUID.
 */
fun String.isUuid(): Boolean = runCatching { this.toUuid() }.isSuccess

/**
 * Converts a string to a UUID.
 */
fun String.toUuid(): UUID = UuidUtils.fromUndashed(this.replace("-", ""))

/**
 * Checks if the string is a valid URL.
 */
fun String.isValidUrl(): Boolean = StringUtil.URL_PATTERN.matches(this)
