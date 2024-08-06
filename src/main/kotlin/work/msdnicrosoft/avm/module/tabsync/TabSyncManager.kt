package work.msdnicrosoft.avm.module.tabsync

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.player.TabListEntry
import net.kyori.adventure.text.Component
import work.msdnicrosoft.avm.util.ComponentUtil.serializer
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

object TabSyncManager {

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

    private val Player.displayName: Component?
        get() = serializer.buildComponent(AVM.config.tabSync.format)
            .replace("%server_name%", currentServer.get().serverInfo.name)
            .replace("%server_nickname%", getServerNickname(currentServer.get().serverInfo.name))
            .replace("%player_name%", username)
            .build()
}
