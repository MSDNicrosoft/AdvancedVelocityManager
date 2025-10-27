package work.msdnicrosoft.avm.util.command.context

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.command.data.PlayerByUUID
import work.msdnicrosoft.avm.util.command.data.component.MiniMessage
import work.msdnicrosoft.avm.util.command.data.server.Server
import work.msdnicrosoft.avm.util.command.data.server.ServerGroup
import work.msdnicrosoft.avm.util.component.ComponentSerializer
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.string.isUuid
import work.msdnicrosoft.avm.util.string.toUuid
import java.util.*

object ArgumentParser {

    fun parseUuid(argument: String): UUID {
        if (!argument.isUuid()) {
            throwCommandException(tr("avm.general.invalid.uuid") { args { string("uuid", argument) } })
        }

        return argument.toUuid()
    }

    fun parsePlayer(argument: String): Player {
        val player: Optional<Player> = server.getPlayer(argument)

        if (player.isEmpty) {
            throwCommandException(tr("avm.general.not_found.player") { args { string("player", argument) } })
        }

        return player.get()
    }

    fun parsePlayerByUuid(argument: String): PlayerByUUID {
        val uuid: UUID = parseUuid(argument)

        val player: Optional<Player> = server.getPlayer(uuid)

        if (player.isEmpty) {
            throwCommandException(tr("avm.general.not_found.player") { args { string("player", argument) } })
        }
        return PlayerByUUID(uuid, player.get())
    }

    fun parseServer(argument: String): Server {
        if (!ConfigManager.config.whitelist.isServerGroup(argument) && server.getServer(argument).isEmpty) {
            throwCommandException(tr("avm.general.not_found.server") { args { string("server", argument) } })
        }

        return Server(argument)
    }

    fun parseRegisteredServer(argument: String): RegisteredServer {
        val registeredServer: Optional<RegisteredServer> = server.getServer(argument)

        if (registeredServer.isEmpty) {
            throwCommandException(tr("avm.general.not_found.server") { args { string("server", argument) } })
        }

        return registeredServer.get()
    }

    fun parseServerGroup(argument: String): ServerGroup {
        if (!ConfigManager.config.whitelist.isServerGroup(argument)) {
            throwCommandException(
                tr("avm.general.not_found.server_group") { args { string("server_group", argument) } }
            )
        }

        return ServerGroup(argument)
    }

    fun parseMiniMessage(argument: String): MiniMessage =
        MiniMessage(ComponentSerializer.MINI_MESSAGE.deserialize(argument))
}
