/**
 * Portions of this code are from artifex
 *
 * https://github.com/InsinuateProjects/artifex/blob/4a30f82e3244ae56bec633e6dd4df9c40b72d300
 * /project/bootstrap-velocity/src/main/kotlin/ink/ptms/artifex/velocityside/ArtifexVelocityAdapter.kt
 */

package work.msdnicrosoft.avm.impl

import com.velocitypowered.api.proxy.Player
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin as VelocityPlugin

@PlatformSide(Platform.VELOCITY)
class VelocityAdapter : PlatformAdapter {
    override fun adaptCommandSender(any: Any): ProxyCommandSender =
        if (any is Player) adaptPlayer(any) else VelocityConsole

    override fun adaptLocation(any: Any): Location = error("Unsupported")

    override fun adaptPlayer(any: Any): ProxyPlayer = VelocityPlayer(any as Player)

    override fun allWorlds(): List<String> = error("Unsupported")

    override fun console(): ProxyCommandSender = VelocityConsole

    override fun onlinePlayers(): List<ProxyPlayer> = VelocityPlugin.server.allPlayers.map { adaptPlayer(it) }

    override fun platformLocation(location: Location): Any = error("Unsupported")
}
