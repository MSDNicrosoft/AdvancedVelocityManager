package work.msdnicrosoft.avm.util.packet.data.codec

interface Encoder<E, D> {
    fun encode(): E
}
