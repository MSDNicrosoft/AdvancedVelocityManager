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

/**
 * Fluent builder for registering or replacing a [com.velocitypowered.proxy.protocol.MinecraftPacket]
 * inside internal packet registries of Velocity.
 *
 *
 * Instances are obtained through [Class] and configured with chained calls such as [direction], [mapping], etc.
 *
 * Once fully configured, call either [register] (for new packets) or [replace] (for packet substitution).
 *
 * @param T concrete packet type that implements [MinecraftPacket]
 */
@Suppress("unused")
class Packet<T : MinecraftPacket> private constructor(private val packet: Class<T>) {
    private lateinit var oldPacket: Class<T>
    private lateinit var direction: Direction
    private lateinit var packetSupplier: Supplier<T>
    private lateinit var stateRegistry: StateRegistry
    private val mappings = mutableListOf<PacketMapping>()

    /**
     * Sets the *old* packet class that will be replaced when [replace] is called.
     *
     * @param packet the class of the packet to be substituted
     * @return this builder for chaining
     */
    fun oldPacket(packet: Class<T>): Packet<T> {
        oldPacket = packet
        return this
    }

    /**
     * Supplies the factory that will create new packet instances when the registry needs them.
     *
     * @param packetSupplier a [Supplier] that returns a new packet instance
     * @return this builder for chaining
     */
    fun packetSupplier(packetSupplier: Supplier<T>): Packet<T> {
        this.packetSupplier = packetSupplier
        return this
    }

    /**
     * Declares the network direction (client-bound or server-bound) for which this packet will be registered.
     *
     * @param direction the communication direction
     * @return this builder for chaining
     */
    fun direction(direction: Direction): Packet<T> {
        this.direction = direction
        return this
    }

    /**
     * Specifies the state registry (hand-shake, status, login, play) in which the packet will live.
     *
     * @param stateRegistry the Velocity state registry
     * @return this builder for chaining
     */
    fun stateRegistry(stateRegistry: StateRegistry): Packet<T> {
        this.stateRegistry = stateRegistry
        return this
    }

    /**
     * Adds a version–id mapping for this packet.
     *
     * @param id         protocol-specific numeric identifier
     * @param from       inclusive protocol version this entry starts from
     * @param to         inclusive protocol version this entry ends at
     * @param encodeOnly if true this mapping is used only for encoding
     * @return this builder for chaining
     */
    fun mapping(id: Int, from: ProtocolVersion, to: ProtocolVersion, encodeOnly: Boolean): Packet<T> {
        mappings.add(Companion.mapping(id, from, to, encodeOnly))
        return this
    }

    /**
     * Adds a version–id mapping that applies from the given protocol version onward (no upper bound).
     *
     * @param id         protocol-specific numeric identifier
     * @param from       inclusive protocol version this entry starts from
     * @param encodeOnly if true this mapping is used only for encoding
     * @return this builder for chaining
     */
    fun mapping(id: Int, from: ProtocolVersion, encodeOnly: Boolean): Packet<T> {
        mappings.add(Companion.mapping(id, from, encodeOnly))
        return this
    }

    /**
     * Adds a collection of [PacketMapping] in bulk.
     *
     * @param mappings the mappings to append
     * @return this builder for chaining
     */
    fun mappings(mappings: Collection<PacketMapping>): Packet<T> {
        this.mappings.addAll(mappings)
        return this
    }

    /**
     * Registers this packet as a **new** entry in the Velocity packet registry.
     *
     * @throws IllegalStateException if no mappings have been provided
     * @throws IllegalStateException if the registry for the chosen direction cannot be located
     */
    fun register() {
        check(mappings.isNotEmpty()) { "You must provide at least one packet mapping" }

        val directionName = direction.name.lowercase()
        val packetRegistry = stateRegistry.getProperty<PacketRegistry>(directionName)
            ?: error("Packet registry not found for $directionName play state")

        packetRegistry.invokeMethod<Nothing>("register", packet, packetSupplier, mappings.toTypedArray())
    }

    /**
     * Replaces an **existing** packet (specified by [oldPacket])
     * in all protocol versions with the new packet class and supplier.
     *
     * This method walks through every protocol registry that contains the old
     * packet and swaps both the numeric id → supplier map and the class → id map.
     *
     * @throws IllegalStateException if the old packet has no entry in any registry, or if internal fields are missing
     */
    fun replace() {
        val versions = packet.getProperty<Map<ProtocolVersion, PacketRegistry.ProtocolRegistry>>("versions")
            ?: error("Packet does not have versions property")

        versions.forEach { protoRegistry ->
            val packetIdToSupplier = protoRegistry
                .getProperty<IntObjectMap<Supplier<out MinecraftPacket>>>("packetIdToSupplier")
                ?: error("Packet does not have packetIdToSupplier property")
            val packetClassToId = protoRegistry
                .getProperty<Object2IntMap<Class<out MinecraftPacket>>>("packetClassToId")
                ?: error("Packet does not have packetClassToId property")

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

        /**
         * Creates a new builder for the given packet class.
         *
         * @param packet Kotlin class object of the packet
         * @param T    packet type
         * @return new [Packet] builder
         */
        fun <T : MinecraftPacket> of(packet: KClass<T>): Packet<T> = Packet(packet.java)

        /**
         * Factory method for a [PacketMapping] that applies from a single protocol version onward.
         *
         * @param id         numeric packet id
         * @param from       inclusive protocol version
         * @param encodeOnly encode-only flag
         * @return a new [PacketMapping]
         */
        fun mapping(id: Int, from: ProtocolVersion, encodeOnly: Boolean): PacketMapping =
            STATE_REGISTRY.invokeMethod<PacketMapping>("map", id, from, encodeOnly, isStatic = true)
                ?: error("Packet mapping not found for id: $id, version: $from")

        /**
         * Factory method for a [PacketMapping] that applies between two protocol versions.
         *
         * @param id         numeric packet id
         * @param from       inclusive protocol version
         * @param to         inclusive protocol version
         * @param encodeOnly encode-only flag
         * @return a new [PacketMapping]
         */
        fun mapping(id: Int, from: ProtocolVersion, to: ProtocolVersion, encodeOnly: Boolean): PacketMapping =
            STATE_REGISTRY.invokeMethod<PacketMapping>("map", id, from, to, encodeOnly, isStatic = true)
                ?: error("Packet mapping not found for id: $id, version: $from - $to")
    }
}
