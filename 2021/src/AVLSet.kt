/**
 * Uses an AVLTree as a map
 */
class AVLSet<T: Comparable<T>> private constructor(private val tree: AVLTree<T>) : Set<T> {
    override val size: Int = tree.size
    override fun contains(element: T) = tree.contains(element)
    override fun containsAll(elements: Collection<T>) = elements.all { this.contains(it) }
    override fun isEmpty() = (tree.size == 0)
    override fun iterator() = tree.scan().iterator()

    // unique ordered interface
    fun scan() = tree.scan()
    fun scanFrom(from: T, inclusive: Boolean = true) = tree.scanFrom(from, inclusive)
    fun scanReversed() = tree.scanReversed()
    fun scanFromReversed(from: T, inclusive: Boolean = true) = tree.scanFromReversed(from, inclusive)

    // modifiers
    fun add(element: T) = AVLSet(tree.add(element))
    fun addAll(elements: Iterable<T>) = AVLSet(tree.addAll(elements))
    fun remove(element: T) = AVLSet(tree.remove(element))
    fun removeAll(elements: Iterable<T>) = AVLSet(tree.removeAll(elements))

    companion object {
        operator fun <T: Comparable<T>> invoke() = AVLSet(AVLTree<T>(compareBy { it }))
    }
}

fun <T: Comparable<T>> Iterable<T>.toAVLSet() = AVLSet<T>().addAll(this)

fun main() {
    val endSet = listOf(1, 7, 5, 4, 3, 2, 1).toAVLSet().add(8).add(1).add(-1)
    println(endSet.scan().toList())
}