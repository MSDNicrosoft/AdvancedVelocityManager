package work.msdnicrosoft.avm.util.i18n

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator
import net.kyori.adventure.translation.GlobalTranslator
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile
import kotlin.io.path.*

object TranslateManager : MiniMessageTranslator() {
    private val name = Key.key("avm")

    private val globalTranslator = GlobalTranslator.translator()

    private val languageFilePath = dataDirectory / "lang"

    private val translations: MutableMap<Locale, ConcurrentHashMap<String, String>> = mutableMapOf()

    private val defaultLocale = Locale.forLanguageTag("en_US")

    fun init() {
        registerTranslations()
        globalTranslator.addSource(this)
    }

    fun disable() {
        globalTranslator.removeSource(this)
    }

    fun reload() {
        translations.clear()
        registerTranslations()
    }

    @Suppress("NestedBlockDepth")
    private fun extractTranslations() {
        val jarUrl = this::class.java.protectionDomain.codeSource?.location ?: return
        JarFile(jarUrl.path).use { jarFile ->
            jarFile.entries().asSequence()
                .filter { it.name.startsWith("lang/") && !it.isDirectory }
                .forEach { entry ->
                    val outPath = languageFilePath / entry.name.removePrefix("lang/")
                    outPath.toFile().parentFile.mkdirs()
                    jarFile.getInputStream(entry).use { input ->
                        FileOutputStream(outPath.toFile()).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
        }
    }

    private fun getLanguageFiles(): List<Path> {
        languageFilePath.toFile().mkdirs()
        return languageFilePath.listDirectoryEntries()
            .filter { it.extension.equals("json", ignoreCase = true) && it.isRegularFile() }
    }

    private fun registerTranslations() {
        if (getLanguageFiles().isEmpty()) {
            extractTranslations()
        }

        for (languageFile in getLanguageFiles()) {
            val locale = Locale.forLanguageTag(languageFile.nameWithoutExtension)
            val currentTranslations = JSON.decodeFromString<Map<String, String>>(languageFile.readTextWithBuffer())
            translations.computeIfAbsent(locale) { ConcurrentHashMap() }.putAll(currentTranslations)
        }
    }

    override fun name(): Key = name

    override fun getMiniMessageString(key: String, locale: Locale): String? {
        val currentLocale = translations[locale]
            ?: translations[Locale.forLanguageTag(locale.language)]
            ?: translations[defaultLocale]
        return currentLocale?.get(key)
    }
}
