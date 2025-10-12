package work.msdnicrosoft.avm.util.collections

/**
 * Adds an [element] to the list if the given condition [predicate] is met.
 *
 * @receiver The list to which the element will be added.
 */
inline fun <E> MutableList<E>.addIf(element: E, predicate: () -> Boolean) {
    if (predicate()) {
        this.add(element)
    }
}

/**
 * Adds a collection of [elements] to the list if the given condition [predicate] is met.
 *
 * @receiver The list to which the collection will be added.
 */
inline fun <E> MutableList<E>.addAllIf(elements: Collection<E>, predicate: () -> Boolean) {
    if (predicate()) {
        this.addAll(elements)
    }
}
