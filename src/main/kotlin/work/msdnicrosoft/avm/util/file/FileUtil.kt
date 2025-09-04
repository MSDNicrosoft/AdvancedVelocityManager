package work.msdnicrosoft.avm.util.file

import com.charleskorn.kaml.AmbiguousQuoteStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.moandjiezana.toml.Toml
import kotlinx.serialization.json.Json

object FileUtil {
    val YAML = Yaml(
        configuration = YamlConfiguration(
            encodeDefaults = true,
            strictMode = false,
            ambiguousQuoteStyle = AmbiguousQuoteStyle.DoubleQuoted
        )
    )

    val JSON = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    val TOML = Toml()
}
