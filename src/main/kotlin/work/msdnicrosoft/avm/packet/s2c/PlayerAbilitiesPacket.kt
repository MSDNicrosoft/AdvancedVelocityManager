package work.msdnicrosoft.avm.packet.s2c

import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.proxy.connection.MinecraftSessionHandler
import com.velocitypowered.proxy.protocol.MinecraftPacket
import com.velocitypowered.proxy.protocol.ProtocolUtils
import io.netty.buffer.ByteBuf

class PlayerAbilitiesPacket(
    flags: List<Flag>,
    val flyingSpeed: Float = 0.05F,
    val fieldOfViewModifier: Float = 0.1F
) : MinecraftPacket {
    constructor() : this(EMPTY)

    private val flags: Int = flags.fold(0) { acc: Int, flag: Flag -> acc or flag.value }

    override fun decode(buf: ByteBuf, direction: ProtocolUtils.Direction, protocolVersion: ProtocolVersion) =
        error("PlayerAbilitiesPacket should not be decoded.")

    override fun encode(buf: ByteBuf, direction: ProtocolUtils.Direction, protocolVersion: ProtocolVersion) {
        buf.writeByte(this.flags)
        buf.writeFloat(this.flyingSpeed)
        buf.writeFloat(this.fieldOfViewModifier)
    }

    override fun handle(sessionHandler: MinecraftSessionHandler?): Boolean = true

    companion object {
        @Suppress("MagicNumber", "unused")
        enum class Flag(val value: Int) {
            EMPTY(0x00),
            INVULNERABLE(0x01),
            FLYING(0x02),
            ALLOW_FLYING(0x04),
            CREATIVE_MODE(0x08)
        }

        val NO_FALLING: List<Flag> = listOf(Flag.FLYING, Flag.ALLOW_FLYING)
        val EMPTY: List<Flag> = listOf(Flag.EMPTY)
    }
}
