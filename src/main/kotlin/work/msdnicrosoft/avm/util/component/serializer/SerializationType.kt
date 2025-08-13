package work.msdnicrosoft.avm.util.component.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

enum class SerializationType : Serializer {
    JSON {
        override val serializer: JSONComponentSerializer by lazy { JSONComponentSerializer.json() }

        // language=json
        override val examples = listOf("""{"color":"red","text":"Hello World!"}""")
    },
    GSON {
        override val serializer: GsonComponentSerializer by lazy { GsonComponentSerializer.gson() }

        // language=json
        override val examples = listOf("""{"color":"red","text":"Hello World!"}""")
    },
    LEGACY_SECTION {
        override val serializer: LegacyComponentSerializer by lazy { LegacyComponentSerializer.legacySection() }
        override val examples = listOf("§aHello World!")
    },
    LEGACY_AMPERSAND {
        override val serializer: LegacyComponentSerializer by lazy { LegacyComponentSerializer.legacyAmpersand() }
        override val examples = listOf("&aHello World!")
    },
    MINI_MESSAGE {
        override val serializer: MiniMessage by lazy { MiniMessage.miniMessage() }
        override val examples = listOf("<red>Hello World!")

        override fun deserialize(text: String, vararg tagResolver: TagResolver): Component =
            serializer.deserialize(text, *tagResolver)
    },
    STYLE_ONLY_MINI_MESSAGE {
        override val serializer: MiniMessage by lazy {
            MiniMessage.builder()
                .tags(
                    TagResolver.builder()
                        .resolver(StandardTags.font())
                        .resolver(StandardTags.color())
                        .resolver(StandardTags.decorations())
                        .resolver(StandardTags.gradient())
                        .resolver(StandardTags.rainbow())
                        .resolver(StandardTags.reset())
                        .resolver(StandardTags.shadowColor())
                        .build()
                ).build()
        }

        override val examples = listOf("<red>Hello World!")

        override fun deserialize(text: String, vararg tagResolver: TagResolver): Component =
            serializer.deserialize(text, *tagResolver)
    },
    PLAIN_TEXT {
        override val serializer: PlainTextComponentSerializer by lazy { PlainTextComponentSerializer.plainText() }
        override val examples = listOf("Hello World!")
    }
}
