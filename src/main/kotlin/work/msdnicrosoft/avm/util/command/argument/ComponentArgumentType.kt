package work.msdnicrosoft.avm.util.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import net.kyori.adventure.text.Component
import work.msdnicrosoft.avm.util.component.ComponentSerializer

class ComponentArgumentType private constructor(private val type: ComponentSerializer) : ArgumentType<Component> {

    @Suppress("unused")
    companion object {
        /** Creates an argument type for JSON format components */
        fun json() = ComponentArgumentType(ComponentSerializer.JSON)

        /** Creates an argument type for Gson-based JSON format components */
        fun gson() = ComponentArgumentType(ComponentSerializer.GSON)

        /** Creates an argument type for legacy section symbol (§) format components */
        fun legacySection() = ComponentArgumentType(ComponentSerializer.LEGACY_SECTION)

        /** Creates an argument type for legacy ampersand (&) format components */
        fun legacyAmpersand() = ComponentArgumentType(ComponentSerializer.LEGACY_AMPERSAND)

        /** Creates an argument type for MiniMessage format components */
        fun miniMessage() = ComponentArgumentType(ComponentSerializer.MINI_MESSAGE)

        /** Creates an argument type for MiniMessage format with style-only features */
        fun styleOnlyMiniMessage() = ComponentArgumentType(ComponentSerializer.STYLE_ONLY_MINI_MESSAGE)

        /** Creates an argument type for plain text components */
        fun plainText() = ComponentArgumentType(ComponentSerializer.PLAIN_TEXT)

        /** Creates an argument type for basic plain text components */
        fun basicPlainText() = ComponentArgumentType(ComponentSerializer.BASIC_PLAIN_TEXT)
    }

    override fun parse(reader: StringReader): Component = type.serializer.deserialize(reader.readUnquotedString())
    override fun getExamples(): Collection<String> = type.examples
}
