package work.msdnicrosoft.avm.util.server

import com.velocitypowered.api.scheduler.ScheduledTask
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.scheduler
import java.util.concurrent.TimeUnit

fun task(
    delayInMillis: Long = 0L,
    repeatInMillis: Long = 0L,
    runnable: Runnable
): ScheduledTask {
    val taskBuilder = scheduler.buildTask(plugin, runnable)

    if (delayInMillis > 0) {
        taskBuilder.delay(delayInMillis, TimeUnit.MILLISECONDS)
    }

    if (repeatInMillis > 0) {
        taskBuilder.repeat(repeatInMillis, TimeUnit.MILLISECONDS)
    }

    return taskBuilder.schedule()
}
