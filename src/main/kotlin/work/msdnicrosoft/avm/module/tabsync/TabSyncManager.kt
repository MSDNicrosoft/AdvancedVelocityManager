package work.msdnicrosoft.avm.module.tabsync

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.player.TabListEntry
import net.kyori.adventure.text.Component
import work.msdnicrosoft.avm.util.ComponentUtil.serializer
import work.msdnicrosoft.avm.util.ConfigUtil.getServerNickname
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

object TabSyncManager {
    fun update(target: Player) {
        target.tabList.clearAll()
        AVM.plugin.server.allPlayers.forEach { player ->
            target.tabList.addEntry(
                TabListEntry.builder()
                    .tabList(target.tabList)
                    .profile(player.gameProfile)
                    .displayName(player.displayName)
                    .latency(player.ping.toInt())
                    .build()
            )
        }
    }

    private val Player.displayName: Component?
        get() = serializer.buildComponent(AVM.config.tabSync.format)
            .replace("%server_name%", currentServer.get().serverInfo.name)
            .replace("%server_nickname%", getServerNickname(currentServer.get().serverInfo.name))
            .replace("%player_name%", username)
            .build()
}
