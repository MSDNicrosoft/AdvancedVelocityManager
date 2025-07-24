/**
 * Portions of this code are modified from lls-manager
 * https://github.com/plusls/lls-manager/blob/master/src/main/java/com/plusls/llsmanager/util/PacketUtil.java
 */

package work.msdnicrosoft.avm.util.packet

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.proxy.protocol.MinecraftPacket
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction
import com.velocitypowered.proxy.protocol.StateRegistry
import com.velocitypowered.proxy.protocol.StateRegistry.HANDSHAKE
import com.velocitypowered.proxy.protocol.StateRegistry.LOGIN
import com.velocitypowered.proxy.protocol.StateRegistry.PLAY
import com.velocitypowered.proxy.protocol.StateRegistry.PacketMapping
import com.velocitypowered.proxy.protocol.StateRegistry.PacketRegistry
import com.velocitypowered.proxy.protocol.StateRegistry.STATUS
import io.netty.util.collection.IntObjectMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import work.msdnicrosoft.avm.util.packet.Packet.Companion.mapping
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
@Suppress("unused", "UnsafeCallOnNullableType")
class Packet<T : MinecraftPacket> private constructor(private val packet: Class<T>) {
    private lateinit var oldPacket: Class<T>
    private lateinit var direction: String
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
        this.direction = direction.name.lowercase()
        return this
    }

    /**
     * Specifies the state registry ([HANDSHAKE], [STATUS], [LOGIN] and [PLAY]) in which the packet will live.
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
     */
    fun register() {
        check(mappings.isNotEmpty()) { "You must provide at least one packet mapping" }

        val packetRegistry = stateRegistry.asResolver()
            .firstField {
                name = direction
                superclass()
            }.get<PacketRegistry>()!!

        packetRegistry.asResolver()
            .firstMethod { name = "register" }
            .invoke(packet, packetSupplier, mappings.toTypedArray())
    }

    /**
     * Replaces an **existing** packet (specified by [oldPacket])
     * in all protocol versions with the new packet class and supplier.
     *
     * This method walks through every protocol registry that contains the old
     * packet and swaps both the numeric id → supplier map and the class → id map.
     */
    fun replace() {
        val packetRegistry = stateRegistry.asResolver()
            .firstField { name = direction }
            .get<PacketRegistry>()!!

        val versions = packetRegistry.asResolver()
            .firstField { name = "versions" }
            .get<Map<ProtocolVersion, PacketRegistry.ProtocolRegistry>>()!!

        val packetIdToSupplierResolver = classOf<PacketRegistry.ProtocolRegistry>().resolve()
            .firstField { name = "packetIdToSupplier" }
        val packetClassToIdResolver = classOf<PacketRegistry.ProtocolRegistry>().resolve()
            .firstField { name = "packetClassToId" }

        versions.values.forEach { protoRegistry ->
            val packetIdToSupplier = packetIdToSupplierResolver.copy()
                .of(protoRegistry)
                .get<IntObjectMap<Supplier<out MinecraftPacket>>>()!!

            val packetClassToId = packetClassToIdResolver.copy()
                .of(protoRegistry)
                .get<Object2IntMap<Class<out MinecraftPacket>>>()!!

            val packetId = packetClassToId.object2IntEntrySet()
                .find { entry -> oldPacket.isAssignableFrom(entry.key) }
                ?.intValue
                ?: return@forEach

            packetIdToSupplier[packetId] = packetSupplier
            packetClassToId[packet] = packetId
        }
    }

    companion object {
        private val STATE_REGISTRY_MAP_METHOD = classOf<StateRegistry>().resolve().firstMethod {
            name = "map"
            parameters(Int::class, ProtocolVersion::class, ProtocolVersion::class, Boolean::class)
        }

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
            mapping(id, from, null, encodeOnly)

        /**
         * Factory method for a [PacketMapping] that applies between two protocol versions.
         *
         * @param id         numeric packet id
         * @param from       inclusive protocol version
         * @param to         inclusive protocol version
         * @param encodeOnly encode-only flag
         * @return a new [PacketMapping]
         */
        fun mapping(id: Int, from: ProtocolVersion, to: ProtocolVersion?, encodeOnly: Boolean): PacketMapping =
            STATE_REGISTRY_MAP_METHOD.invoke<PacketMapping>(id, from, to, encodeOnly)!!
    }
}
