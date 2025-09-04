package work.msdnicrosoft.avm.module.imports

import com.velocitypowered.api.command.CommandSource
import work.msdnicrosoft.avm.module.imports.importers.LlsManagerImporter
import work.msdnicrosoft.avm.module.imports.importers.QuAnVelocityWhitelistImporter

enum class PluginName {
    LLS_MANAGER {
        override fun import(source: CommandSource, defaultServer: String): Boolean =
            LlsManagerImporter.import(source, defaultServer)
    },
    QU_AN_VELOCITYWHITELIST {
        override fun import(source: CommandSource, defaultServer: String): Boolean =
            QuAnVelocityWhitelistImporter.import(source, defaultServer)
    };

    abstract fun import(source: CommandSource, defaultServer: String): Boolean

    companion object {
        val PLUGINS: List<String> by lazy { entries.map { it.name.replace("_", "-").lowercase() } }

        fun of(name: String): PluginName = valueOf(name.replace("-", "_").uppercase())
    }
}
