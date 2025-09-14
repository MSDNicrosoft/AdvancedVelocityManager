package work.msdnicrosoft.avm.util.server

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import com.velocitypowered.api.scheduler.ScheduledTask
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.scheduler
import work.msdnicrosoft.avm.config.ConfigManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

inline val ServerInfo.nickname: String get() = ConfigManager.config.getServerNickName(this.name)

fun task(delayInMillis: Long = 0L, repeatInMillis: Long = 0L, runnable: Runnable): ScheduledTask {
    val taskBuilder = scheduler.buildTask(plugin, runnable)

    if (delayInMillis > 0) {
        taskBuilder.delay(delayInMillis, TimeUnit.MILLISECONDS)
    }

    if (repeatInMillis > 0) {
        taskBuilder.repeat(repeatInMillis, TimeUnit.MILLISECONDS)
    }

    return taskBuilder.schedule()
}

/**
 * Sends a player to a specific server.
 *
 * @param server The server to send the player to.
 * @receiver The player to send.
 */
fun Player.sendToServer(server: RegisteredServer): CompletableFuture<Boolean> =
    this.createConnectionRequest(server).connectWithIndication()
