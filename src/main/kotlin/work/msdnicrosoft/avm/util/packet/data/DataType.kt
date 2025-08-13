package work.msdnicrosoft.avm.util.packet.data

import work.msdnicrosoft.avm.util.packet.data.codec.Encoder

interface DataType<E, D> : Encoder<E, D> {
    override fun encode(): E
}
