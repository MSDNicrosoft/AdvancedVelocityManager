package work.msdnicrosoft.avm.util

import com.charleskorn.kaml.AmbiguousQuoteStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter

object FileUtil {

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
    inline fun <reified T> Yaml.encodeToString(value: T): String = encodeToString(serializersModule.serializer(), value)

    fun Path.readTextWithBuffer() = this.bufferedReader().use { it.readText() }

    fun File.readTextWithBuffer() = this.bufferedReader().use { it.readText() }

    fun Path.writeTextWithBuffer(text: String) = this.bufferedWriter().use { it.write(text) }

    fun File.writeTextWithBuffer(text: String) = this.bufferedWriter().use { it.write(text) }
}
