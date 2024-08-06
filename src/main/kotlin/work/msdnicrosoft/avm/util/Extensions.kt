package work.msdnicrosoft.avm.util

object Extensions {

    /**
     * Adds an element to a list if a condition is met.
     *
     * @param element The element to add.
     * @param block The condition to check.
     */
    fun <T> MutableList<T>.addIf(element: T, block: () -> Boolean) {
        if (block()) add(element)
    }
}
