package work.msdnicrosoft.avm.module.imports.importers

import work.msdnicrosoft.avm.util.command.context.CommandContext

interface Importer {
    val displayName: String

    /**
     * Import data from other plugins
     *
     * @param defaultServer The default server to send players to if they are not online
     * @return Whether the import was successful
     */
    fun import(context: CommandContext, defaultServer: String): Boolean
}
