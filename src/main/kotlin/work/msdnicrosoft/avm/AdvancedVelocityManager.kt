package work.msdnicrosoft.avm

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.util.unsafeLazy
import taboolib.platform.VelocityPlugin

@PlatformSide(Platform.VELOCITY)
object AdvancedVelocityManager : Plugin() {

    val plugin by unsafeLazy { VelocityPlugin.getInstance() }

    val configFile by unsafeLazy { getDataFolder().resolve("config.yml") }

    val configLock = Object()

    var hasFloodgate: Boolean = false
    override fun onEnable() {
        hasFloodgate = plugin.server.pluginManager.getPlugin("floodgate") != null
        plugin.logger.debug("Nya~!")
        loadConfig()

    }



    }





    private fun loadConfig() {

    }

}