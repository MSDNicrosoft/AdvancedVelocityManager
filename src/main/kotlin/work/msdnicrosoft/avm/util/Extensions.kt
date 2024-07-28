package work.msdnicrosoft.avm.util

import com.charleskorn.kaml.Yaml
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.util.UuidUtils
import kotlinx.serialization.serializer
import net.kyori.adventure.text.Component
import java.util.UUID

object Extensions {

    /**
     * Adds an element to a list if a condition is met.
     *
     * @param element The element to add.
     * @param block The condition to check.
     */
    fun <T> MutableList<T>.addIf(element: T, block: () -> Boolean) {
        if (block()) add(element)
    }

    /**
     * Repeats a string a specified number of times.
     *
     * @param n The number of times to repeat the string.
     * @return The repeated string.
     */
    operator fun String.times(n: Int) = this.repeat(n)

    /**
     * Replaces occurrences of multiple strings in a string.
     *
     * @param pairs The pairs of strings to replace.
     * @return The modified string.
     */
    fun String.replace(vararg pairs: Pair<String, String>): String {
        var newString = this
        pairs.forEach { (old, new) -> newString = newString.replace(old, new) }
        return newString
    }

    /**
     * Checks if a string is a valid UUID.
     *
     * @return True if the string is a valid UUID, false otherwise.
     */
    fun String.isUuid() = runCatching { this.toUuid() }.isSuccess

    /**
     * Converts a string to a UUID, removing any dashes.
     *
     * @return The UUID representation of the string.
     */
    fun String.toUuid() = UuidUtils.fromUndashed(this.replace("-", ""))

    /**
     * Converts a [UUID] to a [String], removing any dashes.
     *
     * @return The string representation of the [UUID].
     */
    fun UUID.toUndashedString() = this.toString().replace("-", "")

    /**
     * Decodes a string from YAML format into an object of type [T].
     *
     * @param string The string to decode.
     * @return The decoded object.
     */
    inline fun <reified T> Yaml.decodeFromString(string: String): T =
        decodeFromString(serializersModule.serializer(), string)

    /**
     * Encodes an object of type [T] into a string in YAML format.
     *
     * @param value The object to encode.
     * @return The encoded string.
     */
    inline fun <reified T> Yaml.encodeToString(value: T): String =
        encodeToString(serializersModule.serializer(), value)

    fun String.formated(): Component = ComponentUtil.serializer.parse(this)!!

    fun Player.sendMessage(message: String) = sendMessage(message.formated())
}
