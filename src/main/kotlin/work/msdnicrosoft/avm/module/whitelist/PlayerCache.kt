package work.msdnicrosoft.avm.module.whitelist

import work.msdnicrosoft.avm.config.ConfigManager

/**
 * This object is used to provide a completion source for the `/avmwl add` command by caching player information.
 */
object PlayerCache {
    private inline val config get() = ConfigManager.config.whitelist.cachePlayers

    private val players: MutableSet<String> = mutableSetOf()

    val readOnly: List<String> get() = this.players.toList()

    fun reload() {
        if (config.enabled) this.players.clear()
    }

    fun add(player: String) {
        if (!config.enabled) return

        if (this.players.size >= config.maxSize) {
            this.players.remove(this.players.last())
        }
        this.players.add(player)
    }
}
