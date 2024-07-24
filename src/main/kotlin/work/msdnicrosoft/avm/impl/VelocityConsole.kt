/**
 * Portions of this code are from artifex
 *
 * https://github.com/InsinuateProjects/artifex/blob/4a30f82e3244ae56bec633e6dd4df9c40b72d300
 * /project/bootstrap-velocity/src/main/kotlin/ink/ptms/artifex/velocityside/ArtifexVelocityConsole.kt
 */

package work.msdnicrosoft.avm.impl

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin as VelocityPlugin

@PlatformSide(Platform.VELOCITY)
object VelocityConsole : ProxyCommandSender {

    private val sender = VelocityPlugin.server.consoleCommandSource

    override var isOp: Boolean
        get() = error("Unsupported")
        set(_) = error("Unsupported")

    override val name: String
        get() = "Console"

    override val origin: Any
        get() = sender

    override fun hasPermission(permission: String): Boolean = sender.hasPermission(permission)

    override fun isOnline(): Boolean = true

    override fun performCommand(command: String): Boolean =
        VelocityPlugin.server.commandManager.executeAsync(sender, command).get()

    override fun sendMessage(message: String) =
        sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message))
}