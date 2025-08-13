package work.msdnicrosoft.avm.util.collections

/**
 * Adds an element to the list if the given condition is met.
 *
 * @param element The element to be added to the list.
 * @param predicate A function that returns a boolean value indicating whether the element should be added.
 * @receiver The list to which the element will be added.
 */
inline fun <E> MutableList<E>.addIf(element: E, predicate: () -> Boolean) {
    if (predicate()) {
        this.add(element)
    }
}

/**
 * Adds a collection of elements to the list if the given condition is met.
 * @param element The collection of elements to be added to the list.
 * @param predicate A function that returns a boolean value indicating whether the collection should be added.
 * @receiver The list to which the collection will be added.
 */
inline fun <E> MutableList<E>.addAllIf(element: Collection<E>, predicate: () -> Boolean) {
    if (predicate()) {
        this.addAll(element)
    }
}
