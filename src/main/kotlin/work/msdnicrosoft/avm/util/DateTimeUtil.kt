package work.msdnicrosoft.avm.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeUtil {
    /**
     * Gets the current date and time as a formatted string based on the specified [format] and [time zone][zoneId].
     */
    fun getDateTime(format: String = "yyyy-MM-dd HH:mm:ss", zoneId: ZoneId = ZoneId.systemDefault()): String =
        LocalDateTime.now(zoneId).format(DateTimeFormatter.ofPattern(format))
}
