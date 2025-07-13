package work.msdnicrosoft.avm

import taboolib.common.platform.Platform
import taboolib.module.metrics.Metrics
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

object Metrics {
    private const val PLUGIN_ID = 26491

    fun init() {
        Metrics(PLUGIN_ID, AVM.self.version.get(), Platform.VELOCITY)
    }
}
