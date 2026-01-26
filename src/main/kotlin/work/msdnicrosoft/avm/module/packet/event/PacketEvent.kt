package work.msdnicrosoft.avm.module.packet.event

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.protocol.MinecraftPacket

sealed class PacketEvent(val packet: MinecraftPacket, val player: Player) : ResultedEvent<ResultedEvent.GenericResult> {
    private var result: ResultedEvent.GenericResult = ResultedEvent.GenericResult.allowed()

    override fun getResult(): ResultedEvent.GenericResult = this.result

    override fun setResult(result: ResultedEvent.GenericResult) {
        this.result = result
    }
}
