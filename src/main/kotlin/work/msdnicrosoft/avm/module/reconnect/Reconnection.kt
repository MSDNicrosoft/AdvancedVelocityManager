package work.msdnicrosoft.avm.module.reconnect

import com.velocitypowered.api.event.Continuation
import com.velocitypowered.api.event.player.KickedFromServerEvent
import com.velocitypowered.api.proxy.server.PingOptions
import com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import com.velocitypowered.proxy.protocol.packet.BossBarPacket
import io.netty.channel.EventLoop
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.packet.s2c.PlayerAbilitiesPacket
import work.msdnicrosoft.avm.util.component.builder.title
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

@Suppress("MagicNumber")
class Reconnection(private val event: KickedFromServerEvent, private val continuation: Continuation) {
    private val player = event.player as ConnectedPlayer
    private val scheduledExecutor: EventLoop = player.connection.eventLoop()

    private val pingOptions: PingOptions = PingOptions.builder()
        .timeout(Duration.ofMillis(config.pingTimeout))
        .build()

    private val connectingTitle: Title = title {
        mainTitle { mini(config.message.connecting.title) }
        subTitle { mini(config.message.connecting.subtitle) }
        fadeIn(1L.seconds)
        stay(30L.seconds)
        fadeOut(1L.seconds)
    }

    private val waitingTitle: Title = title {
        mainTitle { mini(config.message.waiting.title) }
        subTitle { mini(config.message.waiting.subtitle) }
        fadeIn(1L.seconds)
        stay(30L.seconds)
        fadeOut(1L.seconds)
    }

    private var state: State = State.WAITING

    init {
        // Prevent player to be kicked by no-flight
        this.player.connection.write(PlayerAbilitiesPacket(PlayerAbilitiesPacket.NO_FALLING))

        this.player.tabList.clearAll()
        clearBossBars()
    }

    fun reconnect() {
        scheduleConnect()
        scheduleSendMessage()
    }

    private fun scheduleConnect() {
        this.scheduledExecutor.schedule(this::connect, config.pingInterval, TimeUnit.MILLISECONDS)
    }

    private fun scheduleSendMessage() {
        this.scheduledExecutor.schedule(this::sendMessage, config.messageInterval, TimeUnit.MILLISECONDS)
    }

    private fun connect() {
        if (this.state == State.CONNECTED) {
            return
        }
        this.event.server.ping(this.pingOptions).whenComplete { _, throwable ->
            if (throwable != null) {
                this.scheduleConnect()
            } else {
                this.scheduledExecutor.execute {
                    this.state = State.CONNECTING
                    this.scheduledExecutor.schedule({
                        this.player.clearTitle()
                        this.event.result = KickedFromServerEvent.RedirectPlayer.create(this.event.server, Component.empty())
                        this.state = State.CONNECTED
                        this.continuation.resume()
                    }, config.reconnectDelay, TimeUnit.MILLISECONDS)
                }
            }
        }
    }

    private fun sendMessage() {
        if (this.state == State.CONNECTED) {
            return
        }

        this.player.showTitle(if (this.state == State.CONNECTING) this.connectingTitle else this.waitingTitle)
        scheduleSendMessage()
    }

    private fun clearBossBars() {
        val sessionHandler = this.player.connection.activeSessionHandler as? ClientPlaySessionHandler ?: return

        sessionHandler.serverBossBars.forEach { bossBar: UUID ->
            this.player.connection.delayedWrite(
                BossBarPacket().apply {
                    uuid = bossBar
                    action = BossBarPacket.REMOVE
                }
            )
        }
        sessionHandler.serverBossBars.clear()
    }

    companion object {
        private enum class State { WAITING, CONNECTING, CONNECTED }

        private inline val config get() = ConfigManager.config.reconnect
    }
}
