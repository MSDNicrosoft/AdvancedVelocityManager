package work.msdnicrosoft.avm.util.command.context

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.command.data.PlayerByUUID
import work.msdnicrosoft.avm.util.command.data.component.MiniMessage
import work.msdnicrosoft.avm.util.command.data.server.Server
import work.msdnicrosoft.avm.util.command.data.server.ServerGroup
import work.msdnicrosoft.avm.util.component.builder.minimessage.miniMessage
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.string.isUuid
import work.msdnicrosoft.avm.util.string.toUuid
import java.util.Optional
import java.util.UUID
import kotlin.reflect.KClass

fun interface ArgumentParser<T> {
    fun parse(argument: String): T

    companion object {
        private val UUIDParser = ArgumentParser<UUID> { argument ->
            if (!argument.isUuid()) {
                throwCommandException(tr("avm.general.invalid.uuid") { args { string("uuid", argument) } })
            }
            argument.toUuid()
        }

        private val PlayerParser = ArgumentParser<Player> { argument ->
            val player: Optional<Player> = server.getPlayer(argument)
            if (player.isEmpty) {
                throwCommandException(tr("avm.general.not_found.player") { args { string("player", argument) } })
            }
            player.get()
        }

        private val PlayerByUUIDParser = ArgumentParser<PlayerByUUID> { argument ->
            val uuid: UUID = UUIDParser.parse(argument)
            val player: Optional<Player> = server.getPlayer(uuid)
            if (player.isEmpty) {
                throwCommandException(tr("avm.general.not_found.player") { args { string("player", argument) } })
            }
            PlayerByUUID(player.get())
        }

        private val ServerParser = ArgumentParser<Server> { argument ->
            if (!ConfigManager.config.whitelist.isServerGroup(argument) && server.getServer(argument).isEmpty) {
                throwCommandException(tr("avm.general.not_found.server") { args { string("server", argument) } })
            }
            Server(argument)
        }

        private val RegisteredServerParser = ArgumentParser<RegisteredServer> { argument ->
            val server: Optional<RegisteredServer> = server.getServer(argument)
            if (server.isEmpty) {
                throwCommandException(tr("avm.general.not_found.server") { args { string("server", argument) } })
            }
            server.get()
        }

        private val ServerGroupParser = ArgumentParser<ServerGroup> { argument ->
            if (!ConfigManager.config.whitelist.isServerGroup(argument)) {
                throwCommandException(
                    tr("avm.general.not_found.server_group") { args { string("server_group", argument) } }
                )
            }
            ServerGroup(argument)
        }

        private val MiniMessageParser = ArgumentParser<MiniMessage> { argument ->
            MiniMessage(miniMessage(argument))
        }

        val parsers: Map<KClass<*>, ArgumentParser<*>> = mapOf(
            UUID::class to UUIDParser,
            Player::class to PlayerParser,
            PlayerByUUID::class to PlayerByUUIDParser,
            Server::class to ServerParser,
            RegisteredServer::class to RegisteredServerParser,
            ServerGroup::class to ServerGroupParser,
            MiniMessage::class to MiniMessageParser,
        )

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T> of(): ArgumentParser<T>? = this.parsers[T::class] as? ArgumentParser<T>
    }
}
