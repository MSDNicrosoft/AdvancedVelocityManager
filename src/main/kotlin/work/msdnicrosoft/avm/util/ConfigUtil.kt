package work.msdnicrosoft.avm.util

import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

object ConfigUtil {
    /**
     * Retrieves the server nickname from the serverMapping configuration.
     * If no mapping is found for the server, returns the original server name.
     *
     * @param server The server name to retrieve the nickname for.
     * @return The server nickname.
     */
    fun getServerNickname(server: String): String =
        ConfigManager.config.serverMapping[server] ?: server

    fun isValidServer(name: String) = AVM.plugin.server.getServer(name).isPresent || isServerGroup(name)

    /**
     * Checks if a server with the given name belongs to a server group.
     *
     * @param name The name of the server to check.
     * @return True if the server belongs to a server group, false otherwise.
     */
    fun isServerGroup(name: String): Boolean =
        name in ConfigManager.config.whitelist.serverGroups.keys

    /**
     * Retrieves a list of servers that belong to the specified group.
     *
     * @param group The name of the server group to retrieve servers for.
     */
    fun getServersInGroup(group: String): List<String> =
        ConfigManager.config.whitelist.serverGroups[group].orEmpty()
}
