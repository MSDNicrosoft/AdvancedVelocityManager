package work.msdnicrosoft.avm.util.command.context

import com.highcapable.kavaref.extension.classOf
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import work.msdnicrosoft.avm.util.command.context.ArgumentParser.parseMiniMessage
import work.msdnicrosoft.avm.util.command.context.ArgumentParser.parsePlayer
import work.msdnicrosoft.avm.util.command.context.ArgumentParser.parsePlayerByUuid
import work.msdnicrosoft.avm.util.command.context.ArgumentParser.parseRegisteredServer
import work.msdnicrosoft.avm.util.command.context.ArgumentParser.parseServer
import work.msdnicrosoft.avm.util.command.context.ArgumentParser.parseServerGroup
import work.msdnicrosoft.avm.util.command.context.ArgumentParser.parseUuid
import work.msdnicrosoft.avm.util.command.data.PlayerByUUID
import work.msdnicrosoft.avm.util.command.data.component.MiniMessage
import work.msdnicrosoft.avm.util.command.data.server.Server
import work.msdnicrosoft.avm.util.command.data.server.ServerGroup
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.TranslatableBuilder
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.component.builder.text.ComponentBuilder
import work.msdnicrosoft.avm.util.component.builder.text.component
import java.util.*
import kotlin.reflect.KProperty
import com.mojang.brigadier.context.CommandContext as BrigadierCommandContext

typealias S = CommandSource

@Suppress("unused")
class CommandContext(val context: BrigadierCommandContext<S>) {
    inline operator fun <reified T : Any> getValue(thisRef: Any?, property: KProperty<*>): T =
        when (classOf<T>()) {
            BrigadierCommandContext::class.java -> this.context
            S::class.java -> this.context.source
            UUID::class.java -> parseUuid(getStringArgument(property.name))
            Player::class.java -> parsePlayer(getStringArgument(property.name))
            PlayerByUUID::class.java -> parsePlayerByUuid(getStringArgument(property.name))
            Server::class.java -> parseServer(getStringArgument(property.name))
            RegisteredServer::class.java -> parseRegisteredServer(getStringArgument(property.name))
            ServerGroup::class.java -> parseServerGroup(getStringArgument(property.name))
            MiniMessage::class.java -> parseMiniMessage(getStringArgument(property.name))
            else -> this.context.getArgument(property.name, T::class.java)
        } as T

    fun sendMessage(message: ComponentLike) = this.context.source.sendMessage(message)

    inline fun sendMessage(
        joinConfiguration: JoinConfiguration = JoinConfiguration.spaces(),
        componentBuilder: ComponentBuilder.() -> Unit
    ) = this.sendMessage(component(joinConfiguration, componentBuilder))

    fun sendPlainMessage(message: String) = this.context.source.sendPlainMessage(message)

    fun sendRichMessage(message: String, vararg resolvers: TagResolver) =
        this.context.source.sendRichMessage(message, *resolvers)

    fun sendTranslatable(key: String, builder: TranslatableBuilder.() -> Unit = {}) = this.sendMessage(tr(key, builder))

    fun getStringArgument(name: String): String = this.context.getArgument(name, String::class.java)
}
