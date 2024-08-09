/**
 * Portions of this code are from TabooLib
 *
 * https://github.com/TabooLib/taboolib/blob/8a998b946c4d4a3a93168cb84a40e31391967713
 * /platform/platform-velocity-impl/src/main/kotlin/taboolib/platform/type/VelocityPlayer.kt
 */

package work.msdnicrosoft.avm.impl

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.title.Title
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyGameMode
import taboolib.common.platform.ProxyParticle
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.common.util.Location
import taboolib.common.util.Vector
import java.net.InetSocketAddress
import java.time.Duration
import java.util.Locale
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin as VelocityPlugin

@PlatformSide(Platform.VELOCITY)
class VelocityPlayer(val player: Player) : ProxyPlayer {
    override var absorptionAmount: Double
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val address: InetSocketAddress? = player.remoteAddress

    override var allowFlight: Boolean
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val attackCooldown: Int
        get() = error("Unsupported")

    override var bedSpawnLocation: Location?
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override var compassTarget: Location
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override var displayName: String?
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override var exhaustion: Float
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override var exp: Float
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val facing: String
        get() = error("Unsupported")

    override val firstPlayed: Long
        get() = error("Unsupported")

    override var flySpeed: Float
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override var foodLevel: Int
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override var gameMode: ProxyGameMode
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override var hasGravity: Boolean
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override var health: Double
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val isBlocking: Boolean
        get() = error("Unsupported")

    override val isConversing: Boolean
        get() = error("Unsupported")

    override val isDead: Boolean
        get() = error("Unsupported")

    override var isFlying: Boolean
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override var isGliding: Boolean
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override var isGlowing: Boolean
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val isInsideVehicle: Boolean
        get() = error("Unsupported")

    override val isLeashed: Boolean
        get() = error("Unsupported")

    override val isOnGround: Boolean
        get() = error("Unsupported")

    override val isRiptiding: Boolean
        get() = error("Unsupported")

    override val isSleeping: Boolean
        get() = error("Unsupported")

    override var isSleepingIgnored: Boolean
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val isSneaking: Boolean
        get() = error("Unsupported")

    override val isSprinting: Boolean
        get() = error("Unsupported")

    override var isSwimming: Boolean
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val lastPlayed: Long
        get() = error("Unsupported")

    override var level: Int
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val locale = player.effectiveLocale?.toString() ?: Locale.ENGLISH.toString()

    override val location: Location
        get() = error("Unsupported")

    override var maxHealth: Double
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val maximumAir: Int
        get() = error("Unsupported")

    override var noDamageTicks: Int
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val ping = player.ping.toInt()

    override var playerListName: String?
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override var playerTime: Long
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val pose: String
        get() = error("Unsupported")

    override var remainingAir: Int
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override var saturation: Float
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val sleepTicks: Int
        get() = error("Unsupported")

    override val uniqueId = player.uniqueId

    override var walkSpeed: Float
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val world: String
        get() = error("Unsupported")

    override fun chat(message: String) = player.spoofChatInput(message)

    override fun giveExp(exp: Int) = error("Unsupported")

    override fun kick(message: String?) = player.disconnect(Component.text(message ?: ""))

    override fun playSound(
        location: Location,
        sound: String,
        volume: Float,
        pitch: Float
    ) = player.playSound(
        Sound.sound(Key.key(sound), Sound.Source.MASTER, volume, pitch),
        location.x,
        location.y,
        location.z
    )

    override fun playSoundResource(
        location: Location,
        sound: String,
        volume: Float,
        pitch: Float
    ) = playSound(location, sound, volume, pitch)

    override fun sendActionBar(message: String) = player.sendActionBar(Component.text(message))

    override fun sendParticle(
        particle: ProxyParticle,
        location: Location,
        offset: Vector,
        count: Int,
        speed: Double,
        data: ProxyParticle.Data?
    ) = error("Unsupported")

    override fun sendRawMessage(message: String) =
        player.sendMessage(GsonComponentSerializer.gson().deserialize(message))

    override fun sendTitle(
        title: String?,
        subtitle: String?,
        fadein: Int,
        stay: Int,
        fadeout: Int
    ) = player.showTitle(
        Title.title(
            Component.text(title ?: ""),
            Component.text(subtitle ?: ""),
            Title.Times.times(
                Duration.ofMillis(fadein * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeout * 50L)
            )
        )
    )

    override fun teleport(location: Location) = error("Unsupported")

    override var isOp: Boolean
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val name = player.username

    override val origin: Any = player

    override fun hasPermission(permission: String) = player.hasPermission(permission)

    override fun isOnline() = onlinePlayers().any { it.name == name }

    override fun performCommand(command: String) =
        VelocityPlugin.server.commandManager.executeAsync(player, command).get()

    override fun sendMessage(message: String) = player.sendMessage(Component.text(message))
}
