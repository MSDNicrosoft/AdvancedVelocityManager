package work.msdnicrosoft.avm.util

class ConfigUtil {
}
import com.charleskorn.kaml.AmbiguousQuoteStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.json.Json

object ConfigUtil {
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
