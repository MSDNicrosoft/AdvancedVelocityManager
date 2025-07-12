package work.msdnicrosoft.avm.module.imports

import taboolib.common.platform.ProxyCommandSender
import work.msdnicrosoft.avm.module.imports.importers.LlsManagerImporter
import work.msdnicrosoft.avm.module.imports.importers.QuAnVelocityWhitelistImporter

enum class PluginName {
    LLS_MANAGER {
        override fun import(sender: ProxyCommandSender, defaultServer: String): Boolean =
            LlsManagerImporter.import(sender, defaultServer)
    },
    QU_AN_VELOCITYWHITELIST {
        override fun import(sender: ProxyCommandSender, defaultServer: String): Boolean =
            QuAnVelocityWhitelistImporter.import(sender, defaultServer)
    };

    companion object {
        val plugins by lazy { entries.map { it.name.replace("_", "-").lowercase() } }

        fun of(name: String): PluginName = valueOf(name.replace("-", "_").uppercase())
    }

    abstract fun import(sender: ProxyCommandSender, defaultServer: String): Boolean
}
