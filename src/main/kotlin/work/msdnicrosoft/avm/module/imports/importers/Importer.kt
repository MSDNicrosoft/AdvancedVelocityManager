package work.msdnicrosoft.avm.module.imports.importers

import com.velocitypowered.api.command.CommandSource

/**
 * Importer interface for importing whitelist data from other plugins
 */
interface Importer {

    /**
     * The name of the plugin to import from
     */
    val pluginName: String

    /**
     * Import whitelist data from other plugins
     *
     * @param defaultServer The default server to send players to if they are not online
     * @return Whether the import was successful
     */
    fun import(source: CommandSource, defaultServer: String): Boolean
}
