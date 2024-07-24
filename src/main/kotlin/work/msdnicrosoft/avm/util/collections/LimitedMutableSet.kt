package work.msdnicrosoft.avm.util.collections

import java.util.function.Predicate

/**
 * A custom implementation of MutableSet that limits the maximum size of the set.
 *
 * @param maxSize The maximum size of the set.
 */
class LimitedMutableSet<T>(private val maxSize: Int) : MutableSet<T> {
    /**
     * The delegate set that holds the actual elements.
     */
    internal val delegate = LinkedHashSet<T>()

    /**
     * Adds the specified element to the set.
     *
     * If the size of the set exceeds the maximum size, the last element is removed.
     *
     * @param element The element to add.
     * @return `true` if the element was added, `false` otherwise.
     */
    override fun add(element: T): Boolean {
        if (size >= (maxSize)) {
            delegate.remove(delegate.last())
        }
        return delegate.add(element)
    }

    /**
     * Adds all the elements in the specified collection to this set.
     *
     * If any of the elements cannot be added, the method returns `false`.
     *
     * @param elements The collection of elements to add.
     * @return `true` if all elements were added, `false` otherwise.
     */
    override fun addAll(elements: Collection<T>): Boolean = elements.fold(true) { acc, element ->
        if (!add(element)) false else acc
    }

    /**
     * Returns the number of elements in the set.
     *
     * @return The number of elements in the set.
     */
    override val size: Int
        get() = delegate.size

    /**
     * Checks if the set contains the specified element.
     *
     * @param element The element to check.
     * @return `true` if the set contains the element, `false` otherwise.
     */
    override fun contains(element: T): Boolean = element in delegate

    /**
     * Checks if the set contains all of the elements in the specified collection.
     *
     * @param elements The collection of elements to check.
     * @return `true` if the set contains all of the elements, `false` otherwise.
     */
    override fun containsAll(elements: Collection<T>): Boolean = delegate.containsAll(elements)

    /**
     * Checks if the set is empty.
     *
     * @return `true` if the set is empty, `false` otherwise.
     */
    override fun isEmpty(): Boolean = delegate.isEmpty()

    /**
     * Returns an iterator over the elements in the set.
     *
     * @return An iterator over the elements in the set.
     */
    override fun iterator(): MutableIterator<T> = delegate.iterator()

    /**
     * Removes the specified element from the set.
     *
     * @param element The element to remove.
     * @return `true` if the element was removed, `false` otherwise.
     */
    override fun remove(element: T): Boolean = delegate.remove(element)

    /**
     * Removes all the elements in the specified collection from the set.
     *
     * @param elements The collection of elements to remove.
     * @return `true` if any of the elements were removed, `false` otherwise.
     */
    override fun removeAll(elements: Collection<T>): Boolean = delegate.removeAll(elements)

    /**
     * Retains only the elements in the set that are also in the specified collection.
     *
     * @param elements The collection of elements to retain.
     * @return `true` if any of the elements were removed, `false` otherwise.
     */
    override fun retainAll(elements: Collection<T>): Boolean = delegate.retainAll(elements)

    /**
     * Removes all the elements from the set.
     */
    override fun clear() = delegate.clear()

    /**
     * Returns a string representation of the set.
     *
     * @return A string representation of the set.
     */
    override fun toString(): String = delegate.toString()

    /**
     * Removes all elements from this set that satisfy the given predicate.
     *
     * @param filter The predicate used to test whether an element should be removed.
     * @return `true` if any elements were removed, `false` otherwise.
     */
    override fun removeIf(filter: Predicate<in T>): Boolean = delegate.removeIf(filter)
}
