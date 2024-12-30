package work.msdnicrosoft.avm.module.tabsync

import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submitAsync
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

@Suppress("unused")
@PlatformSide(Platform.VELOCITY)
object TabSyncHandler {

    private val config
        get() = ConfigManager.config.tabSync

    @SubscribeEvent
    fun onPlayerDisconnect(event: DisconnectEvent) {
        if (config.enabled) {
            submitAsync(delay = 20) {
                AVM.plugin.server.allPlayers.forEach { player ->
                    player.tabList.removeEntry(event.player.uniqueId)
                }
            }
        }
    }

    @SubscribeEvent
    fun onPlayerConnected(event: ServerConnectedEvent) {
        if (config.enabled) {
            submitAsync(delay = 20) {
                val player = event.player
                AVM.plugin.server.allPlayers.forEach { entryPlayer ->
                    if (entryPlayer != player) {
                        TabSyncManager.update(player, entryPlayer)
                    }
                    TabSyncManager.update(entryPlayer, player)
                }
            }
        }
    }
}
