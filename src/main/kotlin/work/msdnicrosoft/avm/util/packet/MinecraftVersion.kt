package work.msdnicrosoft.avm.util.packet

import com.velocitypowered.api.network.ProtocolVersion

@Suppress("unused")
enum class MinecraftVersion {
    UNKNOWN,
    LEGACY,
    MINECRAFT_1_7_2,
    MINECRAFT_1_7_6,
    MINECRAFT_1_8,
    MINECRAFT_1_9,
    MINECRAFT_1_9_1,
    MINECRAFT_1_9_2,
    MINECRAFT_1_9_4,
    MINECRAFT_1_10,
    MINECRAFT_1_11,
    MINECRAFT_1_11_1,
    MINECRAFT_1_12,
    MINECRAFT_1_12_1,
    MINECRAFT_1_12_2,
    MINECRAFT_1_13,
    MINECRAFT_1_13_1,
    MINECRAFT_1_13_2,
    MINECRAFT_1_14,
    MINECRAFT_1_14_1,
    MINECRAFT_1_14_2,
    MINECRAFT_1_14_3,
    MINECRAFT_1_14_4,
    MINECRAFT_1_15,
    MINECRAFT_1_15_1,
    MINECRAFT_1_15_2,
    MINECRAFT_1_16,
    MINECRAFT_1_16_1,
    MINECRAFT_1_16_2,
    MINECRAFT_1_16_3,
    MINECRAFT_1_16_4,
    MINECRAFT_1_17,
    MINECRAFT_1_17_1,
    MINECRAFT_1_18,
    MINECRAFT_1_18_2,
    MINECRAFT_1_19,
    MINECRAFT_1_19_1,
    MINECRAFT_1_19_3,
    MINECRAFT_1_19_4,
    MINECRAFT_1_20,
    MINECRAFT_1_20_2,
    MINECRAFT_1_20_3,
    MINECRAFT_1_20_5,
    MINECRAFT_1_21,
    MINECRAFT_1_21_2,
    MINECRAFT_1_21_4,
    MINECRAFT_1_21_5,
    MINECRAFT_1_21_6,
    MINECRAFT_1_21_7,
    MINECRAFT_1_21_9,
    MINECRAFT_1_21_11;

    companion object {
        fun MinecraftVersion.toProtocolVersion(): ProtocolVersion? = try {
            ProtocolVersion.valueOf(this.name.uppercase())
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
