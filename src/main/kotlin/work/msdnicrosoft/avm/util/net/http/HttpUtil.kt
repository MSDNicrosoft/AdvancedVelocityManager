package work.msdnicrosoft.avm.util.net.http

import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server

object HttpUtil {
    val USER_AGENT =
        "AdvancedVelocityManager/${plugin.self.version.get()} (${server.version.name}/${server.version.version})"
}
