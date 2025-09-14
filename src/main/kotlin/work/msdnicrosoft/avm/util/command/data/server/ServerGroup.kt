package work.msdnicrosoft.avm.util.command.data.server

import work.msdnicrosoft.avm.config.ConfigManager

data class ServerGroup(val name: String) {
    val servers: List<String>
        get() = ConfigManager.config.whitelist.getServersInGroup(this.name)
}
