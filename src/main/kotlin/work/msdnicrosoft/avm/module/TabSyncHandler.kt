package work.msdnicrosoft.avm.module

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.player.TabListEntry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.eventManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.component.ComponentSerializer.MINI_MESSAGE
import work.msdnicrosoft.avm.util.server.task

object TabSyncHandler {
    private inline val config get() = ConfigManager.config.tabSync

    fun init() {
        eventManager.register(plugin, this)
    }

    fun disable() {
        eventManager.unregisterListener(plugin, this)
    }

    @Subscribe
    fun onPlayerDisconnect(event: DisconnectEvent) {
        if (!config.enabled) return

        task {
            server.allPlayers.forEach { player ->
                player.tabList.removeEntry(event.player.uniqueId)
            }
        }
    }

    @Subscribe
    fun onPostPlayerConnected(event: ServerPostConnectEvent) {
        if (!config.enabled) return

        task {
            val player = event.player
            server.allPlayers.forEach { entryPlayer ->
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
        val displayName: Component = entry.displayName
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

    private inline val Player.displayName: Component
        get() = MINI_MESSAGE.deserialize(
            config.format,
            Placeholder.unparsed("server_name", currentServer.get().serverInfo.name),
            Placeholder.parsed(
                "server_nickname",
                ConfigManager.config.getServerNickName(currentServer.get().serverInfo.name)
            ),
            Placeholder.unparsed("player_name", username)
        )
}
