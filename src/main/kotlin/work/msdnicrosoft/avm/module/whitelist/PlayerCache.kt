package work.msdnicrosoft.avm.module.whitelist

import com.velocitypowered.api.event.connection.PreLoginEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.SubscribeEvent
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.config
import work.msdnicrosoft.avm.util.Extensions.toUndashedString
import work.msdnicrosoft.avm.util.collections.LimitedMutableSet

/**
 * Handles caching of players for whitelist functionality within the Advanced Velocity Manager.
 * This object is utilized to provide a completion source for the `whitelist add` command by caching
 * player information. It interacts with the WhitelistManager to check if players are whitelisted and
 * manages a cache of players based on the server configuration.
 */
@PlatformSide(Platform.VELOCITY)
object PlayerCache {

    lateinit var players: LimitedMutableSet<WhitelistManager.Player>

    fun onEnable() {
        players = LimitedMutableSet<WhitelistManager.Player>(config.whitelist.cachePlayers.maxSize)
    }

    @Suppress("unused")
    @SubscribeEvent(postOrder = PostOrder.LAST)
    fun onPlayerPreLogin(event: PreLoginEvent) {
        if (!config.whitelist.cachePlayers.enabled) return

        val hasUuid = event.uniqueId != null
        val isWhitelisted = if (hasUuid) {
            WhitelistManager.isWhitelisted(WhitelistManager.Player(event.username, event.uniqueId!!.toUndashedString()))
        } else {
            WhitelistManager.isWhitelisted(event.username)
        }

        if (isWhitelisted) return
        players.add(WhitelistManager.Player(event.username, event.uniqueId?.toUndashedString() ?: ""))
    }
}
