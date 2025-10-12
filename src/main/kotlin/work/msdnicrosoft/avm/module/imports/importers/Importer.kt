package work.msdnicrosoft.avm.module.imports.importers

import com.velocitypowered.api.command.CommandSource

interface Importer {

    val pluginName: String

    /**
     * Import data from other plugins
     *
     * @param defaultServer The default server to send players to if they are not online
     * @return Whether the import was successful
     */
    fun import(source: CommandSource, defaultServer: String): Boolean
}
