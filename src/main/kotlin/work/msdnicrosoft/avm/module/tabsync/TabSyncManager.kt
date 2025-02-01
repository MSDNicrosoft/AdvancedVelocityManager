package work.msdnicrosoft.avm.module.tabsync

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.player.TabListEntry
import net.kyori.adventure.text.Component
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.util.component.ComponentUtil.serializer

object TabSyncManager {

    /**
     * Updates the tab list entry of the target player with the display name of the entry player.
     *
     * @param target The player whose tab list is being updated.
     * @param entry The player whose display name is being used for the update.
     */
    fun update(target: Player, entry: Player) {
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
        get() = serializer.buildComponent(ConfigManager.config.tabSync.format)
            .replace("%server_name%", currentServer.get().serverInfo.name)
            .replace("%server_nickname%", getServerNickname(currentServer.get().serverInfo.name))
            .replace("%player_name%", username)
            .build()
}
