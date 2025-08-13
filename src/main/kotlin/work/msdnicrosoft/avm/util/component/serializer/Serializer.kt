package work.msdnicrosoft.avm.util.component.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.ComponentSerializer
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger

interface Serializer {
    val serializer: ComponentSerializer<Component, *, String>
    val examples: Collection<String>

    fun deserialize(text: String, vararg tagResolver: TagResolver): Component {
        logger.warn("The MiniMessage tag resolver is not supported in this serialization type.")
        return serializer.deserialize(text)
    }

    fun deserialize(text: String): Component = serializer.deserialize(text)
    fun deserializeOrNull(text: String?): Component? = serializer.deserializeOrNull(text)

    fun serialize(component: Component): String = serializer.serialize(component)
    fun serializeOrNull(component: Component?): String? = serializer.serializeOrNull(component)
}
