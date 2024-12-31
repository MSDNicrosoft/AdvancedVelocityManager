package work.msdnicrosoft.avm.util

import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

object HttpUtil {
    val USER_AGENT =
        "AdvancedVelocityManager/${AVM.self.version} (${plugin.server.version.name}/${plugin.server.version.version})"
}
