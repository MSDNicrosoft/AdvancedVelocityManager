package work.msdnicrosoft.avm.config.data

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MapSync(
    @YamlComment("Xaero's Minimap and World Map sync settings.",)
    val xaero: Xaero = Xaero(),

    @YamlComment(
        "World info sync settings.",
        "This provides support for VoxelMap and JourneyMap.",
    )
    @SerialName("world-info")
    val worldInfo: WorldInfo = WorldInfo(),
) {
    @Serializable
    data class Xaero(
        @YamlComment("Whether to enable Xaero's Minimap synchronization.")
        val mini: Boolean = true,
        @YamlComment("Whether to enable Xaero's WorldMap synchronization.")
        val world: Boolean = true,
    )

    @Serializable
    data class WorldInfo(
        @YamlComment(
            "Whether to enable world info synchronization in legacy packet protocol.",
            "This is scheduled to be removed in the future.",
        )
        val legacy: Boolean = true,

        @YamlComment(
            "Whether to enable world info synchronization in modern packet protocol.",
            "This is required for higher version of VoxelMap and JourneyMap to work properly.",
        )
        val modern: Boolean = true,
    )
}
