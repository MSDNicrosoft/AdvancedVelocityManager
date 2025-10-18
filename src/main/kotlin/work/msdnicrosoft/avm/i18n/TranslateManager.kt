package work.msdnicrosoft.avm.i18n

import com.highcapable.kavaref.extension.classOf
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator
import net.kyori.adventure.translation.GlobalTranslator
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.dataDirectory
import work.msdnicrosoft.avm.AdvancedVelocityManagerPlugin.Companion.logger
import work.msdnicrosoft.avm.util.file.FileUtil.JSON
import work.msdnicrosoft.avm.util.file.readTextWithBuffer
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile
import kotlin.io.path.*

object TranslateManager : MiniMessageTranslator() {
    private val NAME: Key = Key.key("avm")
    private val DEFAULT_LOCALE: Locale = Locale.forLanguageTag("en_US")

    private val globalTranslator: GlobalTranslator = GlobalTranslator.translator()
    private val languageFileDirectory: Path = dataDirectory / "lang"

    private val translations: MutableMap<Locale, ConcurrentHashMap<String, String>> = mutableMapOf()

    fun init() {
        logger.info("Loading language...")
        this.registerTranslations()
        this.globalTranslator.addSource(this)
    }

    fun disable() {
        this.globalTranslator.removeSource(this)
    }

    fun reload() {
        logger.info("Reloading language...")
        this.translations.clear()
        this.registerTranslations()
    }

    override fun name(): Key = this.NAME

    override fun getMiniMessageString(key: String, locale: Locale): String? {
        val currentLocale = this.translations[locale]
            ?: this.translations[Locale.forLanguageTag(locale.language)]
            ?: this.translations[this.DEFAULT_LOCALE]
        return currentLocale?.get(key)
    }

    @Suppress("NestedBlockDepth")
    private fun checkAndUpdateTranslations() {
        val jarUrl = classOf<TranslateManager>().protectionDomain.codeSource?.location ?: return
        JarFile(jarUrl.path).use { jarFile ->
            jarFile.entries().asSequence()
                .filter { entry -> entry.name.startsWith("lang/") && !entry.isDirectory }
                .forEach { entry ->
                    val outPath = (this.languageFileDirectory / entry.name.removePrefix("lang/")).toFile()
                    outPath.parentFile.mkdirs()

                    val fileExists = outPath.exists()
                    val fileContentChanged = if (fileExists) {
                        val localContent = outPath.readTextWithBuffer()
                        val jarContent = jarFile.getInputStream(entry).bufferedReader().use { it.readText() }
                        localContent != jarContent
                    } else {
                        true
                    }

                    if (fileExists && !fileContentChanged) {
                        return@forEach
                    }

                    jarFile.getInputStream(entry).use { input ->
                        FileOutputStream(outPath).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
        }
    }

    private fun getLanguageFiles(): List<Path> {
        this.languageFileDirectory.toFile().mkdirs()
        return this.languageFileDirectory.listDirectoryEntries().filter { entry ->
            entry.extension.equals("json", ignoreCase = true) && entry.isRegularFile()
        }
    }

    private fun registerTranslations() {
        if (this.getLanguageFiles().isEmpty()) {
            this.checkAndUpdateTranslations()
        }

        this.getLanguageFiles().forEach { file ->
            val locale = Locale.forLanguageTag(file.nameWithoutExtension)
            val currentTranslations: Map<String, String> = JSON.decodeFromString(file.readTextWithBuffer())
            this.translations.computeIfAbsent(locale) { ConcurrentHashMap() }.putAll(currentTranslations)
        }
    }
}
