/**
 * Portions of this code are modified from lls-manager
 * https://github.com/plusls/lls-manager/blob/master/src/main/java/com/plusls/llsmanager/util/PacketUtil.java
 */

package work.msdnicrosoft.avm.util.packet

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.highcapable.kavaref.resolver.FieldResolver
import com.highcapable.kavaref.resolver.MethodResolver
import com.velocitypowered.api.network.ProtocolVersion
import com.velocitypowered.proxy.protocol.MinecraftPacket
import com.velocitypowered.proxy.protocol.ProtocolUtils.Direction
import com.velocitypowered.proxy.protocol.StateRegistry
import com.velocitypowered.proxy.protocol.StateRegistry.*
import io.netty.util.collection.IntObjectMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import work.msdnicrosoft.avm.util.packet.MinecraftVersion.Companion.toProtocolVersion
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
 * @param P concrete packet type that implements [MinecraftPacket]
 */
@Suppress("unused")
class Packet<P : MinecraftPacket> private constructor(private val packet: Class<P>) {
    private val mappings: MutableList<PacketMapping?> = mutableListOf()
    private lateinit var packetSupplier: Supplier<P>
    private lateinit var stateRegistry: StateRegistry
    private lateinit var direction: String
    private lateinit var oldPacket: Class<P>

    /**
     * Sets the *old* packet class that will be replaced when [replace] is called.
     *
     * @param packet the class of the packet to be substituted
     * @return this builder for chaining
     */
    fun oldPacket(packet: Class<P>): Packet<P> = this.apply { this.oldPacket = packet }

    /**
     * Supplies the factory that will create new packet instances when the registry needs them.
     *
     * @param packetSupplier a [Supplier] that returns a new packet instance
     * @return this builder for chaining
     */
    fun packetSupplier(packetSupplier: Supplier<P>): Packet<P> = this.apply { this.packetSupplier = packetSupplier }

    /**
     * Declares the network direction (client-bound or server-bound) for which this packet will be registered.
     *
     * @param direction the communication direction
     * @return this builder for chaining
     */
    fun direction(direction: Direction): Packet<P> = this.apply { this.direction = direction.name.lowercase() }

    /**
     * Specifies the state registry ([HANDSHAKE], [STATUS], [LOGIN] and [PLAY]) in which the packet will live.
     *
     * @param stateRegistry the Velocity state registry
     * @return this builder for chaining
     */
    fun stateRegistry(stateRegistry: StateRegistry): Packet<P> = this.apply { this.stateRegistry = stateRegistry }

    /**
     * Adds a version–id mapping for this packet.
     *
     * @param id         protocol-specific numeric identifier
     * @param from       inclusive protocol version this entry starts from
     * @param to         inclusive protocol version this entry ends at
     * @param encodeOnly if true, this mapping is used only for encoding
     * @return this builder for chaining
     */
    fun mapping(id: Int, from: MinecraftVersion, to: MinecraftVersion, encodeOnly: Boolean): Packet<P> =
        this.apply { this.mappings.add(Companion.mapping(id, from, to, encodeOnly)) }

    /**
     * Adds a version–id mapping that applies from the given protocol version onward (no upper bound).
     *
     * @param id         protocol-specific numeric identifier
     * @param from       inclusive protocol version this entry starts from
     * @param encodeOnly if true, this mapping is used only for encoding
     * @return this builder for chaining
     */
    fun mapping(id: Int, from: MinecraftVersion, encodeOnly: Boolean): Packet<P> =
        this.apply { this.mappings.add(Companion.mapping(id, from, encodeOnly)) }

    /**
     * Adds a collection of [PacketMapping] in bulk.
     *
     * @param mappings the mappings to append
     * @return this builder for chaining
     */
    fun mappings(mappings: Collection<PacketMapping?>): Packet<P> = this.apply { this.mappings.addAll(mappings) }

    /**
     * Registers this packet as a **new** entry in the Velocity packet registry.
     *
     * @throws IllegalStateException if no mappings have been provided
     */
    @Suppress("UnsafeCallOnNullableType")
    fun register() {
        require(this.mappings.isNotEmpty()) { "You must provide at least one packet mapping" }

        val packetRegistry: PacketRegistry = this.stateRegistry.asResolver()
            .firstField {
                name = this@Packet.direction
                superclass()
            }.get<PacketRegistry>()!!

        packetRegistry.asResolver()
            .firstMethod { name = "register" }
            .invoke(this.packet, this.packetSupplier, this.mappings.filterNotNull().toTypedArray())
    }

    /**
     * Replaces an **existing** packet (specified by [oldPacket])
     * in all protocol versions with the new packet class and supplier.
     *
     * This method walks through every protocol registry that contains the old
     * packet and swaps both the numeric id → supplier map and the class → id map.
     */
    fun replace() = modify(Action.REPLACE)

    /**
     * Removes an **existing** packet (specified by [packet]) in all protocol versions.
     *
     * This method walks through every protocol registry that contains the target
     * packet and swaps both the numeric id → supplier map and the class → id map.
     */
    fun unregister() = modify(Action.UNREGISTER)

    @Suppress("UnsafeCallOnNullableType")
    private fun modify(action: Action) {
        val packetRegistry: PacketRegistry = this.stateRegistry.asResolver()
            .firstField {
                name = this@Packet.direction
                superclass()
            }.get<PacketRegistry>()!!

        val versions: Map<ProtocolVersion, PacketRegistry.ProtocolRegistry> = packetRegistry.asResolver()
            .firstField { name = "versions" }
            .get<Map<ProtocolVersion, PacketRegistry.ProtocolRegistry>>()!!

        val packetIdToSupplierResolver: FieldResolver<PacketRegistry.ProtocolRegistry> =
            classOf<PacketRegistry.ProtocolRegistry>().resolve()
                .firstField { name = "packetIdToSupplier" }
        val packetClassToIdResolver: FieldResolver<PacketRegistry.ProtocolRegistry> =
            classOf<PacketRegistry.ProtocolRegistry>().resolve()
                .firstField { name = "packetClassToId" }

        versions.values.forEach { protoRegistry ->
            val packetIdToSupplier: IntObjectMap<Supplier<out MinecraftPacket>> = packetIdToSupplierResolver.copy()
                .of(protoRegistry)
                .get<IntObjectMap<Supplier<out MinecraftPacket>>>()!!

            val packetClassToId: Object2IntMap<Class<out MinecraftPacket>> = packetClassToIdResolver.copy()
                .of(protoRegistry)
                .get<Object2IntMap<Class<out MinecraftPacket>>>()!!

            when (action) {
                Action.REPLACE -> {
                    val packetId: Int = packetClassToId.object2IntEntrySet()
                        .find { entry -> this.oldPacket.isAssignableFrom(entry.key) }
                        ?.intValue
                        ?: return@forEach
                    packetIdToSupplier[packetId] = this.packetSupplier
                    packetClassToId[this.packet] = packetId
                }

                Action.UNREGISTER -> {
                    packetIdToSupplier.entries.removeIf { (_, supplier) ->
                        this.packet.isAssignableFrom(supplier.get()::class.java)
                    }
                    packetClassToId.object2IntEntrySet().removeIf { (clazz, _) ->
                        this.packet.isAssignableFrom(clazz)
                    }
                }
            }
        }
    }

    companion object {
        enum class Action { REPLACE, UNREGISTER }

        private val STATE_REGISTRY_MAP_METHOD: MethodResolver<StateRegistry> = classOf<StateRegistry>().resolve()
            .firstMethod {
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
        fun mapping(id: Int, from: MinecraftVersion, encodeOnly: Boolean): PacketMapping? =
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
        @Suppress("UnsafeCallOnNullableType")
        fun mapping(id: Int, from: MinecraftVersion, to: MinecraftVersion?, encodeOnly: Boolean): PacketMapping? {
            return this.STATE_REGISTRY_MAP_METHOD.invoke<PacketMapping>(
                id,
                from.toProtocolVersion() ?: return null,
                to?.toProtocolVersion(),
                encodeOnly
            )!!
        }
    }
}
