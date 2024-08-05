package work.msdnicrosoft.avm.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeUtil {

    fun getDateTime(format: String = "yyyy-MM-dd HH:mm:ss") =
        LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(format))
}