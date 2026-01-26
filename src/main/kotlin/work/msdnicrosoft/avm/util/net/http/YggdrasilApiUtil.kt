package work.msdnicrosoft.avm.util.net.http

import com.velocitypowered.api.util.UuidUtils
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.server
import work.msdnicrosoft.avm.config.ConfigManager
import work.msdnicrosoft.avm.config.data.Whitelist
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID
import kotlin.String

object YggdrasilApiUtil {
    private inline val config: Whitelist get() = ConfigManager.config.whitelist
    private val httpClient: HttpClient = HttpClient.newHttpClient()

    inline val serverIsOnlineMode: Boolean get() = server.configuration.isOnlineMode

    /**
     * Retrieves the username associated with the given [uuid].
     * If the server is in offline mode, it returns null.
     * If the server is online, a query is made to the API to retrieve the username.
     */
    fun getUsername(uuid: UUID): String? {
        if (!this.serverIsOnlineMode) {
            return null
        }

        val request: HttpRequest = HttpRequest.newBuilder()
            .setHeader("User-Agent", HttpUtil.USER_AGENT)
            .uri(URI.create("${config.queryApi.profile.trimEnd('/')}/${UuidUtils.toUndashed(uuid)}"))
            .build()

        return this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { resp: HttpResponse<String> ->
                when (val status: HttpStatus = HttpStatus.fromValue(resp.statusCode())) {
                    HttpStatus.OK -> JSON.parseToJsonElement(resp.body()).jsonObject["name"]?.jsonPrimitive?.content
                    HttpStatus.NOT_FOUND, HttpStatus.NO_CONTENT -> HttpStatus.NOT_FOUND.description
                    HttpStatus.TOO_MANY_REQUESTS -> {
                        logger.warn("Exceeded to the rate limit of Profile API, please retry UUID {}", uuid)
                        null
                    }

                    else -> {
                        logger.warn("Failed to query UUID {}: {} {}", uuid, status.value, status.description)
                        null
                    }
                }
            }.get()
    }

    /**
     * Retrieves the UUID associated with the given [username].
     *
     * @param onlineMode Optional parameter to specify whether to use online mode or not. Defaults to null.
     */
    fun getUuid(username: String, onlineMode: Boolean? = null): String? {
        if (onlineMode == false && !this.serverIsOnlineMode) {
            return UuidUtils.toUndashed(UuidUtils.generateOfflinePlayerUuid(username))
        }

        val request: HttpRequest = HttpRequest.newBuilder()
            .setHeader("User-Agent", HttpUtil.USER_AGENT)
            .uri(URI.create("${config.queryApi.uuid.trimEnd('/')}/$username"))
            .build()

        return this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { resp: HttpResponse<String> ->
                when (val status: HttpStatus = HttpStatus.fromValue(resp.statusCode())) {
                    HttpStatus.OK -> JSON.parseToJsonElement(resp.body()).jsonObject["id"]?.jsonPrimitive?.content
                    HttpStatus.NOT_FOUND -> HttpStatus.NOT_FOUND.description
                    HttpStatus.TOO_MANY_REQUESTS -> {
                        logger.warn("Exceeded to the rate limit of UUID API, please retry username {}", username)
                        null
                    }

                    else -> {
                        logger.warn("Failed to query username {}: {} {}", username, status.value, status.description)
                        null
                    }
                }
            }.get()
    }
}
