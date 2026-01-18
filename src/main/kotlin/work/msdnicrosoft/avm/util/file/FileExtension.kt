package work.msdnicrosoft.avm.util.file

import java.io.File
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter

fun Path.readTextWithBuffer(): String = this.bufferedReader().use { it.readText() }

fun File.readTextWithBuffer(): String = this.bufferedReader().use { it.readText() }

fun Path.writeTextWithBuffer(text: String) = this.bufferedWriter().use { it.write(text) }

fun File.writeTextWithBuffer(text: String) = this.bufferedWriter().use { it.write(text) }
