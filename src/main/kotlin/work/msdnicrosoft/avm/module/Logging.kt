package work.msdnicrosoft.avm.module

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.scheduler.ScheduledTask
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.eventManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.util.DateTimeUtil
import work.msdnicrosoft.avm.util.server.task
import java.io.File
import kotlin.io.path.div

object Logging {
    private val file: File
        get() = (dataDirectory / "logs" / "${DateTimeUtil.getDateTime("yyyy-MM-dd")}.log").toFile()

    private val messages = mutableListOf<String>()

    private lateinit var writeTask: ScheduledTask

    fun init() {
        eventManager.register(plugin, this)
        writeTask = task(repeatInMillis = 5 * 60 * 1000L, runnable = this::write)
    }

    @Suppress("UnusedParameter")
    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        write()
    }

    private fun write() {
        if (messages.isEmpty()) return

        try {
            file.bufferedWriter().use { writer ->
                messages.forEach { message ->
                    writer.appendLine(message)
                }
            }
            messages.clear()
        } catch (e: Exception) {
            logger.warn("Failed to write log file: {}", e.message)
        }
    }

    fun log(message: String) = messages.add("[${DateTimeUtil.getDateTime("HH:mm:ss")}]$message")
}
