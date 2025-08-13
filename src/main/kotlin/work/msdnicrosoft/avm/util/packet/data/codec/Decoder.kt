package work.msdnicrosoft.avm.util.packet.data.codec

interface Decoder<E, D> {
    fun decode(data: E): D
}
