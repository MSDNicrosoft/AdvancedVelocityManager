package work.msdnicrosoft.avm.module.reconnect

import com.velocitypowered.api.event.Continuation
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.proxy.server.PingOptions
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import com.velocitypowered.proxy.protocol.packet.BossBarPacket
import net.kyori.adventure.title.Title
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.packet.s2c.PlayerAbilitiesPacket
import work.msdnicrosoft.avm.util.component.ComponentUtil.serializer
import java.time.Duration
import java.util.concurrent.TimeUnit

@Suppress("MagicNumber")
class Reconnection(private val event: KickedFromServerEvent, private val continuation: Continuation) {
    private val server: RegisteredServer = event.server
    private val player: ConnectedPlayer = event.player as ConnectedPlayer
    private val scheduledExecutor = player.connection.eventLoop()

    private val connectingTitle = Title.title(
        serializer.parse(config.message.connecting.title),
        serializer.parse(config.message.connecting.subtitle),
        Title.Times.times(
            Duration.ofMillis(0L),
            Duration.ofMillis(30_000L),
            Duration.ofMillis(0L)
        )
    )

    private val waitingTitle = Title.title(
        serializer.parse(config.message.waiting.title),
        serializer.parse(config.message.waiting.subtitle),
        Title.Times.times(
            Duration.ofMillis(0L),
            Duration.ofMillis(30_000L),
            Duration.ofMillis(0L)
        )
    )

    private var state = State.WAITING

    init {
        // Prevent player to be kicked by no-falling
        player.connection.write(PlayerAbilitiesPacket(PlayerAbilitiesPacket.NO_FALLING))

        player.tabList.clearAll()
        clearBossBars()
    }

    fun reconnect() {
        scheduleConnect()
        scheduleSendMessage()
    }

    private fun scheduleConnect() {
        scheduledExecutor.schedule(this::connect, config.pingInterval, TimeUnit.MILLISECONDS)
    }

    private fun scheduleSendMessage() {
        scheduledExecutor.schedule(this::sendMessage, config.messageInterval, TimeUnit.MILLISECONDS)
    }

    private fun connect() {
        if (state == State.CONNECTED) return
        server.ping(pingOptions).whenComplete { _, throwable ->
            if (throwable != null) {
                scheduleConnect()
            } else {
                scheduledExecutor.execute {
                    state = State.CONNECTING
                    scheduledExecutor.schedule({
                        player.clearTitle()
                        event.result = KickedFromServerEvent.RedirectPlayer.create(server)
                        state = State.CONNECTED
                        continuation.resume()
                    }, config.reconnectDelay, TimeUnit.MILLISECONDS)
                }
            }
        }
    }

    private fun sendMessage() {
        if (state == State.CONNECTED) return

        player.showTitle(if (state == State.CONNECTING) connectingTitle else waitingTitle)
        scheduleSendMessage()
    }

    private fun clearBossBars() {
        (player.connection.activeSessionHandler as? ClientPlaySessionHandler)?.let { sessionHandler ->
            sessionHandler.serverBossBars.forEach { bossBar ->
                player.connection.delayedWrite(
                    BossBarPacket().apply {
                        uuid = bossBar
                        action = BossBarPacket.REMOVE
                    }
                )
            }
            sessionHandler.serverBossBars.clear()
        }
    }

    companion object {
        enum class State { WAITING, CONNECTING, CONNECTED }

        private val pingOptions = PingOptions.builder()
            .timeout(Duration.ofMillis(config.pingTimeout))
            .build()

        private inline val config
            get() = ConfigManager.config.reconnect
    }
}
