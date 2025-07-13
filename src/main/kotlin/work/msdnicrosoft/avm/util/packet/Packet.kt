/**
 * Portions of this code are modified from lls-manager
 * https://github.com/plusls/lls-manager/blob/master/src/main/java/com/plusls/llsmanager/util/PacketUtil.java
 */

package work.msdnicrosoft.avm.util.packet

import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.proxy.protocol.MinecraftPacket
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction
import com.velocitypowered.proxy.protocol.StateRegistry
import com.velocitypowered.proxy.protocol.StateRegistry.PacketMapping
import com.velocitypowered.proxy.protocol.StateRegistry.PacketRegistry
import io.netty.util.collection.IntObjectMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import java.util.function.Supplier
import kotlin.reflect.KClass

@Suppress("unused")
class Packet<T : MinecraftPacket> private constructor(private val packet: Class<T>) {
    private lateinit var oldPacket: Class<T>
    private lateinit var direction: Direction
    private lateinit var packetSupplier: Supplier<T>
    private lateinit var stateRegistry: StateRegistry
    private val mappings = mutableListOf<PacketMapping>()

    fun oldPacket(packet: Class<T>): Packet<T> {
        oldPacket = packet
        return this
    }

    fun packetSupplier(packetSupplier: Supplier<T>): Packet<T> {
        this.packetSupplier = packetSupplier
        return this
    }

    fun direction(direction: Direction): Packet<T> {
        this.direction = direction
        return this
    }

    fun stateRegistry(stateRegistry: StateRegistry): Packet<T> {
        this.stateRegistry = stateRegistry
        return this
    }

    fun mapping(id: Int, from: ProtocolVersion, to: ProtocolVersion, encodeOnly: Boolean): Packet<T> {
        mappings.add(Companion.mapping(id, from, to, encodeOnly))
        return this
    }

    fun mapping(id: Int, from: ProtocolVersion, encodeOnly: Boolean): Packet<T> {
        mappings.add(Companion.mapping(id, from, encodeOnly))
        return this
    }

    fun mappings(mappings: Collection<PacketMapping>): Packet<T> {
        this.mappings.addAll(mappings)
        return this
    }

    fun register() {
        check(mappings.isNotEmpty()) { "You must provide at least one packet mapping" }

        val directionName = direction.name.lowercase()
        val packetRegistry = stateRegistry.getProperty<PacketRegistry>(directionName)
            ?: throw IllegalArgumentException("Packet registry not found for $directionName play state")

        packetRegistry.invokeMethod<Nothing>("register", packet, packetSupplier, mappings.toTypedArray())
    }

    fun replace() {
        val versions = packet.getProperty<Map<ProtocolVersion, PacketRegistry.ProtocolRegistry>>("versions")
            ?: throw IllegalArgumentException("Packet does not have versions property")

        versions.forEach { protoRegistry ->
            val packetIdToSupplier = protoRegistry
                .getProperty<IntObjectMap<Supplier<out MinecraftPacket>>>("packetIdToSupplier")
                ?: throw IllegalArgumentException("Packet does not have packetIdToSupplier property")
            val packetClassToId = protoRegistry
                .getProperty<Object2IntMap<Class<out MinecraftPacket>>>("packetClassToId")
                ?: throw IllegalArgumentException("Packet does not have packetClassToId property")

            val packetId = packetClassToId.object2IntEntrySet()
                .find { entry -> oldPacket.isAssignableFrom(entry.key) }
                ?.intValue

            if (packetId != null) {
                packetIdToSupplier.put(packetId, packetSupplier)
                packetClassToId.put(packet, packetId)
            }
        }
    }

    companion object {
        private val STATE_REGISTRY = StateRegistry::class.java

        fun <T : MinecraftPacket> of(packet: KClass<T>): Packet<T> = Packet(packet.java)

        fun mapping(id: Int, from: ProtocolVersion, encodeOnly: Boolean): PacketMapping =
            STATE_REGISTRY.invokeMethod<PacketMapping>("map", id, from, encodeOnly, isStatic = true)
                ?: throw IllegalArgumentException("Packet mapping not found for id: $id, version: $from")

        fun mapping(id: Int, from: ProtocolVersion, to: ProtocolVersion, encodeOnly: Boolean): PacketMapping =
            STATE_REGISTRY.invokeMethod<PacketMapping>("map", id, from, to, encodeOnly, isStatic = true)
                ?: throw IllegalArgumentException("Packet mapping not found for id: $id, version: $from - $to")
    }
}
