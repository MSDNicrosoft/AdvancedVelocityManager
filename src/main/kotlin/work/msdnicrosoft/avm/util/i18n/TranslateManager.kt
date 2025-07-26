package work.msdnicrosoft.avm.util.i18n

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator
import net.kyori.adventure.translation.GlobalTranslator
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.plugin
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.nameWithoutExtension

object TranslateManager : MiniMessageTranslator() {
    private val name = Key.key("avm")

    private val globalTranslator = GlobalTranslator.translator()

    private val languageFilePath = plugin.configDirectory.resolve("lang")

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

    private fun registerTranslations() {
        languageFilePath.listDirectoryEntries()
            .asSequence()
            .filter { it.extension.equals("json", ignoreCase = true) && it.isRegularFile() }
            .forEach { languageFile ->
                val locale = Locale.forLanguageTag(languageFile.nameWithoutExtension)
                val currentTranslations = JSON.decodeFromString<Map<String, String>>(languageFile.readTextWithBuffer())
                translations.computeIfAbsent(locale) { ConcurrentHashMap() }.putAll(currentTranslations)
            }
    }

    override fun name(): Key = name

    @Suppress("UnsafeCallOnNullableType")
    override fun getMiniMessageString(key: String, locale: Locale): String? =
        (
            translations[locale]
                ?: translations[Locale.forLanguageTag(locale.language)]
                ?: translations[defaultLocale]
            )!![key]
}
