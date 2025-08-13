package work.msdnicrosoft.avm.util.net.netty

import io.netty.buffer.ByteBuf

/**
 * Executes the given [block] on this [ByteBuf] and **always** releases it afterward, even if an exception is thrown.
 *
 *
 * This is a Kotlin-style [kotlin.use] extension that mimics Java’s try-with-resources
 *
 * The receiver buffer is passed **into** the block
 * so that you can perform any read/write operations while retaining full control over its life-cycle.
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
 * @param T  concrete subtype of [ByteBuf]
 * @param R  return type of the supplied block
 * @param block  lambda to execute; receives the buffer as its argument
 * @return the value returned by [block]
 * @throws Exception any exception thrown by [block]; the buffer is still released
 */
inline fun <T : ByteBuf, R> T.use(block: (T) -> R): R =
    try {
        block(this)
    } finally {
        release()
    }

/**
 * Executes the given [block] as a receiver-style lambda on this [ByteBuf]
 * and **always** releases it afterward, even on exception.
 *
 * Unlike [kotlin.use], the buffer is **not** consumed by the block;
 * instead, the block operates on the receiver (`this`)
 * and the original buffer is returned to the caller after release.
 *
 * This is handy when you need to perform side-effect–only operations (e.g. decoding)
 * and still need the buffer reference for logging or further processing.
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
 * @param T  concrete subtype of [ByteBuf]
 * @param R  return type of the supplied block (ignored)
 * @param block  lambda to execute; the buffer is the receiver
 * @return the same [ByteBuf] instance, now with `refCnt == 0`
 */
inline fun <T : ByteBuf, R> T.useApply(block: T.() -> R): T =
    try {
        block()
        this
    } finally {
        release()
    }

/**
 * Executes the given [block] as a receiver-style lambda on this [ByteBuf]
 * and **always** releases it afterward, even on exception.
 *
 *
 * Similar to [use], but the block is invoked with the buffer as the receiver (`this`) rather than as a parameter.
 *
 * The value returned by the block is propagated to the caller while the buffer is released.
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
 * @param T  concrete subtype of [ByteBuf]
 * @param R  return type of the supplied block
 * @param block  lambda to execute; the buffer is the receiver
 * @return the value returned by [block]
 * @throws Exception any exception thrown by [block]; the buffer is still released
 */
inline fun <T : ByteBuf, R> T.useThenApply(block: T.() -> R): R =
    try {
        block()
    } finally {
        release()
    }
