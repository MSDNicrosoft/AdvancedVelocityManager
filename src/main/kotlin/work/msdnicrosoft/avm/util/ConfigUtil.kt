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
    fun getServerNickname(server: String) = ConfigManager.config.serverMapping[server] ?: server

    fun isValidServer(name: String) = AVM.plugin.server.getServer(name).isPresent || isServerGroup(name)

    fun isServerGroup(name: String) = name in ConfigManager.config.whitelist.serverGroups.keys

    fun getServersInGroup(group: String) = ConfigManager.config.whitelist.serverGroups[group]
}
