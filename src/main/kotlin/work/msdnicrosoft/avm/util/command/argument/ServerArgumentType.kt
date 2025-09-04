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
import work.msdnicrosoft.avm.util.component.tr
import java.util.concurrent.CompletableFuture

class ServerArgumentType private constructor(private val serverType: ServerType) : ArgumentType<String> {

    override fun parse(reader: StringReader): String =
        when (this.serverType) {
            ServerType.REGISTERED -> parseRegistered(reader)
            ServerType.GROUP -> parseGroup(reader)
            ServerType.ALL -> parseAll(reader)
        }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val suggestions: Collection<String> = when (this.serverType) {
            ServerType.REGISTERED -> getRegisteredServerNames()
            ServerType.GROUP -> getServerGroupNames().toList()
            ServerType.ALL -> {
                val serverNames: Set<String> = getRegisteredServerNames()
                val groupNames: Set<String> = getServerGroupNames()
                serverNames + groupNames
            }
        }

        val input: String = builder.remaining.lowercase()
        suggestions.filter { it.lowercase().startsWith(input) }
            .forEach(builder::suggest)

        return builder.buildFuture()
    }

    @Suppress("unused")
    companion object {
        private enum class ServerType {
            REGISTERED {
                override val examples: Collection<String> get() = getRegisteredServerNames()
            },
            GROUP {
                override val examples: Collection<String> get() = getServerGroupNames()
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
            val serverName: String = reader.readUnquotedString()
            if (server.getServer(serverName).isEmpty) {
                throw SERVER_NOT_FOUND.createWithContext(reader)
            }

            return serverName
        }

        private fun parseGroup(reader: StringReader): String {
            val groupName: String = reader.readUnquotedString()
            if (!ConfigManager.config.whitelist.isServerGroup(groupName)) {
                throw SERVER_GROUP_NOT_FOUND.createWithContext(reader)
            }

            return groupName
        }

        private fun parseAll(reader: StringReader): String {
            val name: String = reader.readUnquotedString()
            return if (ConfigManager.config.whitelist.isServerGroup(name) || server.getServer(name).isPresent) {
                name
            } else {
                throw SERVER_NOT_FOUND.createWithContext(reader)
            }
        }

        private fun getRegisteredServerNames(): Set<String> = server.allServers.map { it.serverInfo.name }.toSet()

        private fun getServerGroupNames(): Set<String> = ConfigManager.config.whitelist.serverGroups.keys
    }
}
