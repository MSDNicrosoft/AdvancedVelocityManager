package work.msdnicrosoft.avm.util.file

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.serializer
import java.io.File
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter

inline fun <reified T> Yaml.decodeFromString(string: String): T =
    decodeFromString(serializersModule.serializer(), string)

inline fun <reified T> Yaml.encodeToString(value: T): String =
    encodeToString(serializersModule.serializer(), value)

@Suppress("NOTHING_TO_INLINE")
inline fun Path.readTextWithBuffer(): String = this.bufferedReader().use { it.readText() }

@Suppress("NOTHING_TO_INLINE")
inline fun File.readTextWithBuffer(): String = this.bufferedReader().use { it.readText() }

@Suppress("NOTHING_TO_INLINE")
inline fun Path.writeTextWithBuffer(text: String) = this.bufferedWriter().use { it.write(text) }

@Suppress("NOTHING_TO_INLINE")
inline fun File.writeTextWithBuffer(text: String) = this.bufferedWriter().use { it.write(text) }
