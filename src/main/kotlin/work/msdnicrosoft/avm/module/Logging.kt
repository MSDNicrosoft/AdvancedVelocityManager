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
import kotlin.time.Duration.Companion.minutes

object Logging {
    private val file: File get() = (dataDirectory / "logs" / "${DateTimeUtil.getDateTime("yyyy-MM-dd")}.log").toFile()

    private val messages: MutableList<String> = mutableListOf()

    private lateinit var writeTask: ScheduledTask

    fun init() {
        eventManager.register(plugin, this)
        this.writeTask = task(repeat = 5.minutes, runnable = this::write)
    }

    @Suppress("UnusedParameter")
    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        this.write()
    }

    private fun write() {
        if (this.messages.isEmpty()) return

        try {
            this.file.bufferedWriter().use { writer ->
                this.messages.forEach { message ->
                    writer.appendLine(message)
                }
            }
            this.messages.clear()
        } catch (e: Exception) {
            logger.warn("Failed to write log file: {}", e.message)
        }
    }

    fun log(message: String) = this.messages.add("[${DateTimeUtil.getDateTime("HH:mm:ss")}]$message")
}
