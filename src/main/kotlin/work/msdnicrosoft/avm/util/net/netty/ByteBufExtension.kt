package work.msdnicrosoft.avm.util.net.netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil

/**
 * Executes the given [block] on [this][ByteBuf] and **always** releases it afterward, even if an exception is thrown.
 *
 * Example usage:
 * ```
 * val answer = buffer.use { buf ->
 *     buf.writeIntLE(42)
 *     buf.readIntLE()
 * }
 * // buffer has been released here; answer == 42
 * ```
 *
 * @return the value returned by [block]
 * @throws Exception any exception thrown by [block]; the buffer is still released
 */
inline fun <T : ByteBuf, R> T.use(block: (T) -> R): R =
    try {
        block(this)
    } finally {
        this.release()
    }

/**
 * Executes the given [block] as a receiver-style lambda on [this][ByteBuf]
 * and **always** releases it afterward, even on exception.
 *
 * Example usage:
 * ```
 * buffer.useApply {
 *     skipBytes(4)
 *     writeByte(0xFF)
 * }.also { released ->
 *     println("Buffer released? ${released.refCnt() == 0}") // true
 * }
 * ```
 *
 * @return the same [ByteBuf] instance, now with `refCnt == 0`
 */
inline fun <T : ByteBuf, R> T.useApply(block: T.() -> R): T =
    try {
        this.block()
        this
    } finally {
        this.release()
    }

/**
 * Executes the given [block] as a receiver-style lambda on [this][ByteBuf]
 * and **always** releases it afterward, even on exception.
 *
 *
 * Example usage:
 * ```
 * val crc = buffer.useThenApply {
 *     val array = ByteArray(readableBytes())
 *     readBytes(array)
 *     CRC32().let { it.update(array); it.value }
 * }
 * // buffer released, crc contains the checksum
 * ```
 *
 * @return the value returned by [block]
 * @throws Exception any exception thrown by [block]; the buffer is still released
 */
inline fun <T : ByteBuf, R> T.useThenApply(block: T.() -> R): R =
    try {
        this.block()
    } finally {
        this.release()
    }

inline fun <reified T : ByteBuf> T.toByteArray(): ByteArray = ByteBufUtil.getBytes(this)
