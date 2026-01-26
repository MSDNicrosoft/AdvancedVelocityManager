package work.msdnicrosoft.avm.module.packet

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import io.netty.channel.ChannelHandler
import io.netty.channel.DefaultChannelPipeline
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.eventManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.plugin
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.module.packet.handler.PlayerPacketHandler
import work.msdnicrosoft.avm.util.server.task

object PacketHandler {
    const val KEY = "avm-packet-handler"

    fun init() {
        eventManager.register(plugin, this)

        task {
            server.allPlayers.forEach { player ->
                (player as ConnectedPlayer).injectHandler()
            }
        }
    }

    fun disable() {
        eventManager.unregisterListener(plugin, this)

        task {
            server.allPlayers.forEach { player ->
                (player as ConnectedPlayer).removeHandler()
            }
        }
    }

    @Subscribe
    fun onPostLogin(event: PostLoginEvent): EventTask = EventTask.withContinuation { continuation ->
        (event.player as ConnectedPlayer).injectHandler()
        continuation.resume()
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent): EventTask? {
        if (event.loginStatus == DisconnectEvent.LoginStatus.CONFLICTING_LOGIN) {
            return null
        }

        return EventTask.async {
            (event.player as ConnectedPlayer).removeHandler()
        }
    }

    private fun ConnectedPlayer.injectHandler() {
        this.removeHandler()
        this.connection.channel.pipeline().addBefore("handler", KEY, PlayerPacketHandler(this))
    }

    private fun ConnectedPlayer.removeHandler() {
        val pipeline: DefaultChannelPipeline = this.connection.channel.pipeline() as? DefaultChannelPipeline ?: return
        pipeline.removeIfExists<ChannelHandler>(KEY)
    }
}
