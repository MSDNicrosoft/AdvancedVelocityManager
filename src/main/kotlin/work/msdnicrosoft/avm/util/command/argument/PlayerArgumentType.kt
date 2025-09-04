package work.msdnicrosoft.avm.util.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.VelocityBrigadierMessage
import com.velocitypowered.api.proxy.Player
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.util.component.tr
import work.msdnicrosoft.avm.util.string.isUuid
import work.msdnicrosoft.avm.util.string.toUuid
import java.util.*
import java.util.concurrent.CompletableFuture

class PlayerArgumentType private constructor(private val valueType: ValueType) : ArgumentType<Player> {

    @Suppress("unused")
    companion object {
        private enum class ValueType {
            NAME {
                override val examples: Collection<String> = setOf("Player", "0123")
            },
            UUID {
                override val examples: Collection<String> = setOf("dd12be42-52a9-4a91-a8a1-11c01849e498")
            },
            ALL {
                override val examples: Collection<String> = NAME.examples + UUID.name
            };

            abstract val examples: Collection<String>
        }

        private val PLAYER_NOT_FOUND =
            SimpleCommandExceptionType(VelocityBrigadierMessage.tooltip(tr("avm.general.not.found.player")))

        private val INVALID_UUID =
            SimpleCommandExceptionType(VelocityBrigadierMessage.tooltip(tr("avm.general.invalid.uuid")))

        /**
         * Creates a player argument type that accepts both names and UUIDs
         */
        fun all(): PlayerArgumentType = PlayerArgumentType(ValueType.ALL)

        /**
         * Creates a player argument type that only accepts player names
         */
        fun name(): PlayerArgumentType = PlayerArgumentType(ValueType.NAME)

        /**
         * Creates a player argument type that only accepts UUIDs
         */
        fun uuid(): PlayerArgumentType = PlayerArgumentType(ValueType.UUID)
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val input: String = builder.remaining.lowercase()

        when (this.valueType) {
            ValueType.NAME -> {
                server.allPlayers
                    .filter { it.username.lowercase().startsWith(input) }
                    .forEach { builder.suggest(it.username) }
            }

            ValueType.UUID -> {
                server.allPlayers
                    .filter { it.uniqueId.toString().startsWith(input) }
                    .forEach { builder.suggest(it.uniqueId.toString()) }
            }

            ValueType.ALL -> {
                server.allPlayers.forEach { player ->
                    if (player.username.lowercase().startsWith(input)) {
                        builder.suggest(player.username)
                    }
                    if (player.uniqueId.toString().startsWith(input)) {
                        builder.suggest(player.uniqueId.toString())
                    }
                }
            }
        }

        return builder.buildFuture()
    }

    override fun getExamples(): Collection<String> = this.valueType.examples

    private fun parseName(reader: StringReader): Player {
        val playerName: String = reader.readUnquotedString()
        return server.getPlayer(playerName)
            .orElseThrow { PLAYER_NOT_FOUND.createWithContext(reader) }
    }

    override fun parse(reader: StringReader): Player =
        when (this.valueType) {
            ValueType.NAME -> parseName(reader)
            ValueType.UUID -> parseUuid(reader)
            ValueType.ALL -> parseAll(reader)
        }

    private fun parseUuid(reader: StringReader): Player {
        val playerUuid: String = reader.readUnquotedString()

        if (!playerUuid.isUuid()) throw INVALID_UUID.createWithContext(reader)

        return server.getPlayer(playerUuid.toUuid())
            .orElseThrow { PLAYER_NOT_FOUND.createWithContext(reader) }
    }

    private fun parseAll(reader: StringReader): Player {
        val input: String = reader.readUnquotedString()

        val player: Optional<Player> = if (input.isUuid()) {
            server.getPlayer(input.toUuid())
        } else {
            server.getPlayer(input)
        }

        return player.orElseThrow { PLAYER_NOT_FOUND.createWithContext(reader) }
    }
}
