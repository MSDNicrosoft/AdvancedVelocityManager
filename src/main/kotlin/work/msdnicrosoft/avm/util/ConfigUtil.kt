package work.msdnicrosoft.avm.util

import work.msdnicrosoft.avm.config.ConfigManager

object ConfigUtil {
    /**
     * Retrieves the server nickname from the serverMapping configuration.
     * If no mapping is found for the server, returns the original server name.
     *
     * @param server The server name to retrieve the nickname for.
     * @return The server nickname.
     */
    fun getServerNickname(server: String) = ConfigManager.config.serverMapping[server] ?: server

    fun isServerGroupName(server: String) = server in ConfigManager.config.whitelist.serverGroups.keys

    fun getServersInGroup(group: String) = ConfigManager.config.whitelist.serverGroups[group]
}
