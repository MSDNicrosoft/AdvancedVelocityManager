package work.msdnicrosoft.avm.util.component

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.flattener.ComponentFlattener
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.ComponentSerializer
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger

@Suppress("unused")
enum class ComponentSerializer {
    JSON {
        override val serializer: JSONComponentSerializer by lazy { JSONComponentSerializer.json() }

        // language=json
        override val examples: Collection<String> = setOf("""{"color":"red","text": "Hello World!"}""")
    },
    GSON {
        override val serializer: GsonComponentSerializer by lazy { GsonComponentSerializer.gson() }

        // language=json
        override val examples: Collection<String> = setOf("""{"color":"red","text": "Hello World!"}""")
    },
    LEGACY_SECTION {
        override val serializer: LegacyComponentSerializer by lazy { LegacyComponentSerializer.legacySection() }
        override val examples: Collection<String> = setOf("§aHello World!")
    },
    LEGACY_AMPERSAND {
        override val serializer: LegacyComponentSerializer by lazy { LegacyComponentSerializer.legacyAmpersand() }
        override val examples: Collection<String> = setOf("&aHello World!")
    },
    MINI_MESSAGE {
        override val serializer: MiniMessage by lazy { MiniMessage.miniMessage() }
        override val examples: Collection<String> = setOf("<red>Hello World!")

        override fun deserialize(text: String, vararg tagResolver: TagResolver): Component =
            this.serializer.deserialize(text, *tagResolver)
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

        override val examples: Collection<String> = setOf("<red>Hello World!")

        override fun deserialize(text: String, vararg tagResolver: TagResolver): Component =
            this.serializer.deserialize(text, *tagResolver)
    },
    PLAIN_TEXT {
        override val serializer: PlainTextComponentSerializer by lazy { PlainTextComponentSerializer.plainText() }
    },
    BASIC_PLAIN_TEXT {
        override val serializer: PlainTextComponentSerializer by lazy {
            PlainTextComponentSerializer.builder()
                .flattener(ComponentFlattener.basic())
                .build()
        }
    };

    abstract val serializer: ComponentSerializer<Component, *, String>

    open val examples: Collection<String> = setOf("Hello World!")

    open fun deserialize(text: String, vararg tagResolver: TagResolver): Component {
        logger.warn("The MiniMessage tag resolver is not supported in this serialization type.")
        return this.serializer.deserialize(text)
    }

    open fun deserialize(text: String): Component = this.serializer.deserialize(text)
    open fun deserializeOrNull(text: String?): Component? = this.serializer.deserializeOrNull(text)

    open fun serialize(component: Component): String = this.serializer.serialize(component)
    open fun serializeOrNull(component: Component?): String? = this.serializer.serializeOrNull(component)
}
