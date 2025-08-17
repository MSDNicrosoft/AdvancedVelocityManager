package work.msdnicrosoft.avm.util.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.VelocityBrigadierMessage
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.util.ConfigUtil.isServerGroup
import work.msdnicrosoft.avm.util.component.tr
import java.util.concurrent.CompletableFuture

class ServerArgumentType private constructor(private val serverType: ServerType) : ArgumentType<String> {

    @Suppress("unused")
    companion object {
        private enum class ServerType {
            REGISTERED {
                override val examples
                    get() = getRegisteredServerNames()
            },
            GROUP {
                override val examples
                    get() = getServerGroupNames()
            },
            ALL {
                override val examples: Collection<String> = REGISTERED.examples + GROUP.examples
            };

            abstract val examples: Collection<String>
        }

        private val SERVER_NOT_FOUND =
            SimpleCommandExceptionType(VelocityBrigadierMessage.tooltip(tr("avm.general.not.found.server")))

        private val SERVER_GROUP_NOT_FOUND =
            SimpleCommandExceptionType(VelocityBrigadierMessage.tooltip(tr("avm.general.not.found.servergroup")))

        /** Creates a server argument type that only accepts registered server names */
        fun registered() = ServerArgumentType(ServerType.REGISTERED)

        /** Creates a server argument type that only accepts server group names */
        fun group() = ServerArgumentType(ServerType.GROUP)

        /** Creates a server argument type that accepts both registered servers and server groups */
        fun all() = ServerArgumentType(ServerType.ALL)

        private fun parseRegistered(reader: StringReader): String {
            val serverName = reader.readUnquotedString()
            if (server.getServer(serverName).isEmpty) {
                throw SERVER_NOT_FOUND.createWithContext(reader)
            }

            return serverName
        }

        private fun parseGroup(reader: StringReader): String {
            val groupName = reader.readUnquotedString()
            if (!isServerGroup(groupName)) {
                throw SERVER_GROUP_NOT_FOUND.createWithContext(reader)
            }

            return groupName
        }

        private fun parseAll(reader: StringReader): String {
            val name = reader.readUnquotedString()
            return if (isServerGroup(name) || server.getServer(name).isPresent) {
                name
            } else {
                throw SERVER_NOT_FOUND.createWithContext(reader)
            }
        }

        private fun getRegisteredServerNames(): Set<String> = server.allServers.map { it.serverInfo.name }.toSet()

        private fun getServerGroupNames(): Set<String> = ConfigManager.config.whitelist.serverGroups.keys
    }

    override fun parse(reader: StringReader): String = when (serverType) {
        ServerType.REGISTERED -> parseRegistered(reader)
        ServerType.GROUP -> parseGroup(reader)
        ServerType.ALL -> parseAll(reader)
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val suggestions = when (serverType) {
            ServerType.REGISTERED -> getRegisteredServerNames()
            ServerType.GROUP -> getServerGroupNames().toList()
            ServerType.ALL -> {
                val serverNames = getRegisteredServerNames()
                val groupNames = getServerGroupNames()
                serverNames + groupNames
            }
        }

        val input = builder.remaining.lowercase()
        suggestions.filter { it.lowercase().startsWith(input) }
            .forEach(builder::suggest)

        return builder.buildFuture()
    }
}
