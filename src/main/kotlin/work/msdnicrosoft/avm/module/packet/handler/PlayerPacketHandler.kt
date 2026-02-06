package work.msdnicrosoft.avm.module.packet.handler

import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import com.velocitypowered.proxy.protocol.MinecraftPacket
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.eventManager
import work.msdnicrosoft.avm.module.packet.event.ReceivePacketEvent
import work.msdnicrosoft.avm.module.packet.event.SendPacketEvent

class PlayerPacketHandler(private val player: ConnectedPlayer) : ChannelDuplexHandler() {
    override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
        if (packet !is MinecraftPacket) {
            super.channelRead(ctx, packet)
            return
        }

        val allowed: Boolean = eventManager
            .fire(ReceivePacketEvent(packet, this.player))
            .handle { event, throwable ->
                if (throwable != null) {
                    false
                } else {
                    event.result.isAllowed
                }
            }.join()

        if (allowed) {
            super.channelRead(ctx, packet)
        }
    }

    override fun write(ctx: ChannelHandlerContext, packet: Any, promise: ChannelPromise) {
        if (packet !is MinecraftPacket) {
            super.write(ctx, packet, promise)
            return
        }

        val allowed: Boolean = eventManager
            .fire(SendPacketEvent(packet, this.player))
            .handle { event, throwable ->
                if (throwable != null) {
                    false
                } else {
                    event.result.isAllowed
                }
            }.join()

        if (allowed) {
            super.write(ctx, packet, promise)
        }
    }
}
