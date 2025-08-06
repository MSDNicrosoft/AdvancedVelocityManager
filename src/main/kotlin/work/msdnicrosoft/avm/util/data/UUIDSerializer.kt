package work.msdnicrosoft.avm.util.data

import com.velocitypowered.api.util.UuidUtils
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(UuidUtils.toUndashed(value))

    override fun deserialize(decoder: Decoder): UUID = UuidUtils.fromUndashed(decoder.decodeString())
}
