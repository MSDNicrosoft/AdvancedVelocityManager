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
import java.util.function.Supplier
import kotlin.reflect.KClass

/**
 * Fluent builder for registering Velocity [Packet][com.velocitypowered.proxy.protocol.MinecraftPacket]
 * in [Velocity Packet Registry][com.velocitypowered.proxy.protocol.StateRegistry].
 */
@Suppress("unused")
class Packet<P : MinecraftPacket> private constructor(private val packet: Class<P>) {
    private val mappings: MutableList<PacketMapping?> = mutableListOf()
    private lateinit var packetSupplier: Supplier<P>
    private lateinit var stateRegistry: StateRegistry
    private lateinit var packetRegistry: PacketRegistry
    private lateinit var direction: String
    private lateinit var oldPacket: Class<P>

    /**
     * Sets the [*old* packet class][packet] that will be replaced when [replace] is called.
     */
    fun oldPacket(packet: Class<P>): Packet<P> = this.apply { this.oldPacket = packet }

    /**
     * Sets the [supplier][packetSupplier] that will create new packet instances when the registry needs them.
     */
    fun packetSupplier(packetSupplier: Supplier<P>): Packet<P> = this.apply { this.packetSupplier = packetSupplier }

    /**
     * Sets the [network direction][direction] for which this packet will be registered.
     */
    fun direction(direction: Direction): Packet<P> = this.apply { this.direction = direction.name.lowercase() }

    /**
     * Sets the [state registry][stateRegistry] in which the packet will live.
     */
    @Suppress("UnsafeCallOnNullableType")
    fun stateRegistry(stateRegistry: StateRegistry): Packet<P> = this.apply {
        this.stateRegistry = stateRegistry
        this.packetRegistry = this.stateRegistry.asResolver()
            .firstField {
                name = this@Packet.direction
                superclass()
            }.get<PacketRegistry>()!!
    }

    /**
     * Adds a version–id mapping for this packet.
     *
     * @param id protocol-specific numeric identifier
     * @param from inclusive protocol version this entry starts from
     * @param to inclusive protocol version this entry ends at
     * @param encodeOnly if true, this mapping is used only for encoding
     */
    fun mapping(id: Int, from: MinecraftVersion, to: MinecraftVersion, encodeOnly: Boolean): Packet<P> = this.apply {
        this.mappings.add(Companion.mapping(id, from, to, encodeOnly))
    }

    /**
     * Adds a version–id mapping that applies from the given protocol version onward (no upper bound).
     *
     * @param id protocol-specific numeric identifier
     * @param from inclusive protocol version this entry starts from
     * @param encodeOnly if true, this mapping is used only for encoding
     */
    fun mapping(id: Int, from: MinecraftVersion, encodeOnly: Boolean): Packet<P> = this.apply {
        this.mappings.add(Companion.mapping(id, from, encodeOnly))
    }

    /**
     * Adds a collection of [PacketMapping] in bulk.
     *
     * @param mappings the mappings to append
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

        this.packetRegistry.asResolver()
            .firstMethod { name = "register" }
            .invoke(this.packet, this.packetSupplier, this.mappings.filterNotNull().toTypedArray())
    }

    /**
     * Replaces an **existing** packet (specified by [oldPacket])
     * in all protocol versions with the new packet class and supplier.
     */
    fun replace() = modify(Action.REPLACE)

    /**
     * Removes an **existing** packet (specified by [packet]) in all protocol versions.
     */
    fun unregister() = modify(Action.UNREGISTER)

    @Suppress("UnsafeCallOnNullableType")
    private fun modify(action: Action) {
        val versions: Map<ProtocolVersion, PacketRegistry.ProtocolRegistry> = this.packetRegistry.asResolver()
            .firstField { name = "versions" }
            .get<Map<ProtocolVersion, PacketRegistry.ProtocolRegistry>>()!!

        versions.values.forEach { protoRegistry ->
            val packetIdToSupplier: IntObjectMap<Supplier<out MinecraftPacket>> = PACKET_ID_TO_SUPPLIER_RESOLVER.copy()
                .of(protoRegistry)
                .get<IntObjectMap<Supplier<out MinecraftPacket>>>()!!

            val packetClassToId: Object2IntMap<Class<out MinecraftPacket>> = PACKET_CLASS_TO_ID_RESOLVER.copy()
                .of(protoRegistry)
                .get<Object2IntMap<Class<out MinecraftPacket>>>()!!

            when (action) {
                Action.REPLACE -> {
                    val packetId: Int = packetClassToId.object2IntEntrySet()
                        .find { (clazz, _) -> this.oldPacket.isAssignableFrom(clazz) }
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
        private enum class Action { REPLACE, UNREGISTER }

        private val STATE_REGISTRY_MAP_METHOD: MethodResolver<StateRegistry> by lazy {
            classOf<StateRegistry>().resolve()
                .firstMethod {
                    name = "map"
                    parameters(Int::class, ProtocolVersion::class, ProtocolVersion::class, Boolean::class)
                }
        }

        private val PACKET_ID_TO_SUPPLIER_RESOLVER: FieldResolver<PacketRegistry.ProtocolRegistry> by lazy {
            classOf<PacketRegistry.ProtocolRegistry>().resolve()
                .firstField { name = "packetIdToSupplier" }
        }

        private val PACKET_CLASS_TO_ID_RESOLVER: FieldResolver<PacketRegistry.ProtocolRegistry> by lazy {
            classOf<PacketRegistry.ProtocolRegistry>().resolve()
                .firstField { name = "packetClassToId" }
        }

        /**
         * Creates a new builder for the given [packet] class of [T].
         */
        fun <T : MinecraftPacket> of(packet: KClass<T>): Packet<T> = Packet(packet.java)

        /**
         * Factory method for a [PacketMapping] that applies from a single protocol version onward.
         *
         * @param id numeric packet id
         * @param from inclusive protocol version
         * @param encodeOnly encode-only flag
         */
        fun mapping(id: Int, from: MinecraftVersion, encodeOnly: Boolean): PacketMapping? =
            mapping(id, from, null, encodeOnly)

        /**
         * Factory method for a [PacketMapping] that applies between two protocol versions.
         *
         * @param id numeric packet id
         * @param from inclusive protocol version
         * @param to inclusive protocol version
         * @param encodeOnly encode-only flag
         */
        @Suppress("UnsafeCallOnNullableType")
        fun mapping(id: Int, from: MinecraftVersion, to: MinecraftVersion?, encodeOnly: Boolean): PacketMapping? {
            return this.STATE_REGISTRY_MAP_METHOD.invoke<PacketMapping>(
                id,
                from.toProtocolVersion() ?: return null,
                to?.toProtocolVersion(),
                encodeOnly
            )
        }
    }
}
