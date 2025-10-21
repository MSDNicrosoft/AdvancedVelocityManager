package work.msdnicrosoft.avm.util.server

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import com.velocitypowered.api.scheduler.ScheduledTask
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.scheduler
import work.msdnicrosoft.avm.config.ConfigManager
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration
import kotlin.time.toJavaDuration

inline val ServerInfo.nickname: String get() = ConfigManager.config.getServerNickName(this.name)

/**
 * Creates a scheduled [task][runnable] with optional [delay] and [repeat] intervals.
 */
fun task(delay: Duration = Duration.ZERO, repeat: Duration = Duration.ZERO, runnable: Runnable): ScheduledTask {
    val taskBuilder = scheduler.buildTask(plugin, runnable)

    if (delay > Duration.ZERO) {
        taskBuilder.delay(delay.toJavaDuration())
    }

    if (repeat > Duration.ZERO) {
        taskBuilder.repeat(delay.toJavaDuration())
    }

    return taskBuilder.schedule()
}

/**
 * Sends a player to a specific [server].
 */
fun Player.sendToServer(server: RegisteredServer): CompletableFuture<Boolean> =
    this.createConnectionRequest(server).connectWithIndication()
