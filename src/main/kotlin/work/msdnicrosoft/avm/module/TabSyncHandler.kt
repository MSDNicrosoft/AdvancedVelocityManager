package work.msdnicrosoft.avm.module

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.player.TabListEntry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import taboolib.common.platform.function.submitAsync
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.ConfigUtil
import work.msdnicrosoft.avm.util.component.ComponentUtil

object TabSyncHandler {

    private inline val config
        get() = ConfigManager.config.tabSync

    fun init() {
        plugin.server.eventManager.register(plugin, this)
    }

    fun disable() {
        plugin.server.eventManager.unregisterListener(plugin, this)
    }

    @Subscribe
    fun onPlayerDisconnect(event: DisconnectEvent) {
        if (!config.enabled) return

        submitAsync(delay = 20) {
            plugin.server.allPlayers.forEach { player ->
                player.tabList.removeEntry(event.player.uniqueId)
            }
        }
    }

    @Subscribe
    fun onPlayerConnected(event: ServerConnectedEvent) {
        if (!config.enabled) return

        submitAsync(delay = 20) {
            val player = event.player
            plugin.server.allPlayers.forEach { entryPlayer ->
                if (entryPlayer != player) {
                    this@TabSyncHandler.update(player, entryPlayer)
                }
                this@TabSyncHandler.update(entryPlayer, player)
            }
        }
    }

    /**
     * Updates the tab list entry of the target player with the display name of the entry player.
     *
     * @param target The player whose tab list is being updated.
     * @param entry The player whose display name is being used for the update.
     */
    private fun update(target: Player, entry: Player) {
        val displayName = entry.displayName
        target.tabList.getEntry(entry.uniqueId).ifPresentOrElse(
            { it.setDisplayName(displayName) },
            {
                target.tabList.addEntry(
                    TabListEntry.builder()
                        .tabList(target.tabList)
                        .profile(entry.gameProfile)
                        .displayName(displayName)
                        .latency(entry.ping.toInt())
                        .build()
                )
            }
        )
    }

    private inline val Player.displayName: Component?
        get() = ComponentUtil.miniMessage.deserialize(
            config.format,
            Placeholder.unparsed("server_name", currentServer.get().serverInfo.name),
            Placeholder.unparsed("server_nickname", ConfigUtil.getServerNickname(currentServer.get().serverInfo.name)),
            Placeholder.unparsed("player_name", username)
        )
}
