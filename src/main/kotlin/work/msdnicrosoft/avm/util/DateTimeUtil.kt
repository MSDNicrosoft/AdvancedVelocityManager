package work.msdnicrosoft.avm.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeUtil {

    /**
     * Returns a string representation of the current date and time in the specified format and time zone.
     *
     * @param format The format of the date and time string. Defaults to "yyyy-MM-dd HH:mm:ss".
     * @param zoneId The time zone to use. Defaults to the system default time zone.
     *
     * @return A string representation of the current date and time in the specified format and time zone.
     */
    fun getDateTime(format: String = "yyyy-MM-dd HH:mm:ss", zoneId: ZoneId = ZoneId.systemDefault()): String =
        LocalDateTime.now(zoneId).format(DateTimeFormatter.ofPattern(format))
}
