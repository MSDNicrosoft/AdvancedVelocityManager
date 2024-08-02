package work.msdnicrosoft.avm.util

import com.charleskorn.kaml.AmbiguousQuoteStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.json.Json
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin as AVM

object ConfigUtil {
    /**
     * Retrieves the server nickname from the serverMapping configuration.
     * If no mapping is found for the server, returns the original server name.
     *
     * @param server The server name to retrieve the nickname for.
     * @return The server nickname.
     */
    fun getServerNickname(server: String) = AVM.config.serverMapping[server] ?: server

    fun isServerGroupName(server: String) = server in AVM.config.whitelist.serverGroups.keys

    fun getServersInGroup(group: String) = AVM.config.whitelist.serverGroups[group]

    val yaml = Yaml(
        configuration = YamlConfiguration(
            encodeDefaults = true,
            strictMode = false,
            ambiguousQuoteStyle = AmbiguousQuoteStyle.DoubleQuoted
        )
    )

    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
}
