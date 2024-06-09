package work.msdnicrosoft.avw

import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.Plugin
import taboolib.common.platform.event.PostOrder
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info
import taboolib.common.platform.function.unregisterListener
import taboolib.common.util.unsafeLazy
import taboolib.platform.VelocityPlugin

@PlatformSide(Platform.VELOCITY)
object AdvancedVelocityWhitelist : Plugin() {


    val plugin by unsafeLazy { VelocityPlugin.getInstance() }

    override fun onEnable() {
        info("Welcome to use AdvancedVelocityWhitelist")
    }

    @SubscribeEvent(postOrder = PostOrder.EARLY)
    fun onPreLogin(event: PreLoginEvent) {
        // Blocked by other plugins
        if (!event.result.isAllowed) return

        // Whitelist is off
        // if (!config.enabled) return

        val username = event.username

        // if
        event.result = PreLoginEvent.PreLoginComponentResult
            .denied(Component.translatable("multiplayer.disconnect.not_whitelisted").color(NamedTextColor.RED))

    }

    @SubscribeEvent(postOrder = PostOrder.EARLY)
    fun onServerPreConnect(event: ServerPreConnectEvent) {
        // Blocked by other plugins
        if (event.result.server.isEmpty) return

        // Whitelist is off
        // if (!config.enabled) return

        val serverName = event.result.server.get().serverInfo.name
        val player = event.player

        // if
        event.result = ServerPreConnectEvent.ServerResult.denied()
        player.sendMessage(Component.translatable("multiplayer.disconnect.not_whitelisted").color(NamedTextColor.RED))

    }

}