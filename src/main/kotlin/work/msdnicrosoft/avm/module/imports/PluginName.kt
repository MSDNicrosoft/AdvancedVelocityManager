package work.msdnicrosoft.avm.module.imports

import work.msdnicrosoft.avm.module.imports.importers.LlsManagerImporter
import work.msdnicrosoft.avm.module.imports.importers.QuAnVelocityWhitelistImporter
import work.msdnicrosoft.avm.util.command.context.CommandContext

enum class PluginName {
    LLS_MANAGER {
        override fun import(context: CommandContext, defaultServer: String): Boolean =
            LlsManagerImporter.import(context, defaultServer)
    },
    QU_AN_VELOCITYWHITELIST {
        override fun import(context: CommandContext, defaultServer: String): Boolean =
            QuAnVelocityWhitelistImporter.import(context, defaultServer)
    };

    abstract fun import(context: CommandContext, defaultServer: String): Boolean

    companion object {
        val PLUGINS: List<String> by lazy { entries.map { it.name.replace("_", "-").lowercase() } }

        fun of(name: String): PluginName = valueOf(name.replace("-", "_").uppercase())
    }
}
