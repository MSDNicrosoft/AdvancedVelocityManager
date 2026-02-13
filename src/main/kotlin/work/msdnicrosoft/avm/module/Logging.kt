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
import java.util.Collections
import kotlin.io.path.div
import kotlin.time.Duration.Companion.minutes

object Logging {
    private val file: File get() = (dataDirectory / "logs" / "${DateTimeUtil.getDateTime("yyyy-MM-dd")}.log").toFile()

    private val messages: MutableList<String> = Collections.synchronizedList(mutableListOf<String>())

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

    @Suppress("TooGenericExceptionCaught")
    private fun write() {
        if (this.messages.isEmpty()) return

        val snapshot: List<String>
        synchronized(this.messages) {
            snapshot = this.messages.toList()
            this.messages.clear()
        }

        try {
            this.file.parentFile?.mkdirs()
            this.file.bufferedWriter(Charsets.UTF_8).use { writer ->
                snapshot.forEach { message ->
                    writer.appendLine(message)
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to write log file: {}", e.message)
            // Re-add messages that failed to write
            synchronized(this.messages) {
                this.messages.addAll(0, snapshot)
            }
        }
    }

    fun log(message: String) = this.messages.add("[${DateTimeUtil.getDateTime("HH:mm:ss")}]$message")
}
