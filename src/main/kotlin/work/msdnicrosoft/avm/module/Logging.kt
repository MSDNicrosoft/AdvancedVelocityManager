package work.msdnicrosoft.avm.module

import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.getDataFolder
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.logger
import work.msdnicrosoft.avm.util.DateTimeUtil
import java.io.File

object Logging {
    private val LOG_DIR by lazy { File(getDataFolder(), "logs") }

    private val messages = mutableListOf<String>()

    @Suppress("unused")
    @Schedule(delay = 15 * 20L, period = 60 * 20L, async = true)
    @Awake(LifeCycle.DISABLE)
    private fun write() {
        val file = newFile(
            LOG_DIR,
            "${DateTimeUtil.getDateTime("yyyy-MM-dd")}.log",
            create = true
        )

        try {
            file.bufferedWriter().use { writer ->
                messages.forEach { message ->
                    writer.appendLine(message)
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to write log file: {}", e.message)
        }
        messages.clear()
    }

    fun log(message: String) {
        messages.add("[${DateTimeUtil.getDateTime("HH:mm:ss")}]$message")
    }
}
