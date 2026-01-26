package work.msdnicrosoft.avm.module.packet.event

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.protocol.MinecraftPacket

class SendPacketEvent(packet: MinecraftPacket, player: Player) : PacketEvent(packet, player)
