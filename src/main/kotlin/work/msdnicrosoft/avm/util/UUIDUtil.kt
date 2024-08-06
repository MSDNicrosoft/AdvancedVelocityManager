package work.msdnicrosoft.avm.util

import java.util.UUID

object UUIDUtil {
    /**
     * Converts a [UUID] to a [String], removing any dashes.
     *
     * @return The string representation of the [UUID].
     */
    fun UUID.toUndashedString() = this.toString().replace("-", "")
}
