package work.msdnicrosoft.avm.util.command.builder

import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

class SuggestionsScope internal constructor(private val builder: SuggestionsBuilder) {
    fun suggest(text: String): SuggestionsScope {
        if (text.regionMatches(0, this.builder.remaining, 0, this.builder.remaining.length, ignoreCase = true)) {
            this.builder.suggest(text)
        }
        return this
    }

    fun suggest(value: Int): SuggestionsScope {
        val text: String = value.toString()
        if (text.regionMatches(0, this.builder.remaining, 0, this.builder.remaining.length, ignoreCase = true)) {
            this.builder.suggest(value)
        }
        return this
    }

    fun buildFuture(): CompletableFuture<Suggestions> = this.builder.buildFuture()
}
