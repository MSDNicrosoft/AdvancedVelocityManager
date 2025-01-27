package work.msdnicrosoft.avm.module.whitelist

import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import work.msdnicrosoft.avm.config.ConfigManager

/**
 * Handles caching of players for whitelist functionality within the Advanced Velocity Manager.
 * This object is utilized to provide a completion source for the `whitelist add` command by caching
 * player information. It interacts with the WhitelistManager to check if players are whitelisted and
 * manages a cache of players based on the server configuration.
 */
@PlatformSide(Platform.VELOCITY)
object PlayerCache {

    private val config
        get() = ConfigManager.config.whitelist.cachePlayers

    private val players = mutableSetOf<String>()

    val readOnly
        get() = players.toList()

    fun reload() {
        if (!config.enabled) return

        players.clear()
    }

    fun add(player: String) {
        if (!config.enabled) return

        if (players.size >= config.maxSize) {
            players.remove(players.last())
        }
        players.add(player)
    }
}
