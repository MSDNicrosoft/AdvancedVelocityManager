package work.msdnicrosoft.avm.module.whitelist

import work.msdnicrosoft.avm.config.ConfigManager

/**
 * Handles caching of players for whitelist functionality within the Advanced Velocity Manager.
 * This object is used to provide a completion source for the `whitelist add` command by caching
 * player information. It interacts with the WhitelistManager to check if players are whitelisted and
 * manages a cache of players based on the server configuration.
 */
object PlayerCache {
    private inline val config
        get() = ConfigManager.config.whitelist.cachePlayers

    private val players = mutableSetOf<String>()

    val readOnly: List<String>
        get() = players.toList()

    fun reload() {
        if (config.enabled) players.clear()
    }

    fun add(player: String) {
        if (!config.enabled) return

        if (players.size >= config.maxSize) {
            players.remove(players.last())
        }
        players.add(player)
    }
}
