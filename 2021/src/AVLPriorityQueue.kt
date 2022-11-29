import kotlin.Comparator

class AVLPriorityQueue<T: Any> private constructor (private val tree: AVLTree<T>) : Iterable<T> {

    override fun iterator(): Iterator<T> = tree.scan().iterator()

    fun push(item: T) = AVLPriorityQueue(tree.add(item))
    fun pushAll(items: Iterable<T>) = AVLPriorityQueue(tree.addAll(items))
    fun removeLeft(): Pair<T?, AVLPriorityQueue<T>> {
        return when (val itemToRemove = tree.scan().firstOrNull()) {
            null -> Pair(null, this)
            else -> Pair(itemToRemove, AVLPriorityQueue(tree.remove(itemToRemove)))
        }
    }
    fun removeRight(): Pair<T?, AVLPriorityQueue<T>> {
        return when (val itemToRemove = tree.scanReversed().firstOrNull()) {
            null -> Pair(null, this)
            else -> Pair(itemToRemove, AVLPriorityQueue(tree.remove(itemToRemove)))
        }
    }

    companion object {
        operator fun <T: Comparable<T>> invoke() = AVLPriorityQueue(AVLTree<T>(compareBy { it }))
        operator fun <T: Any> invoke(comparator: Comparator<T>) = AVLPriorityQueue(AVLTree(comparator))
    }
}

fun main() {
    val queue = AVLPriorityQueue<Pair<Int, String>>(compareBy { it.first })
        .push(Pair(1, "Bood"))
        .push(Pair(2, "Doody"))
        .push(Pair(1, "Howdy"))
    println(queue.toList())

    val (leftItem, rightQueue) = queue.removeLeft()
    println("${leftItem}, ${rightQueue.toList()}")
    val (rightItem, leftQueue) = queue.removeRight()
    println("${rightItem}, ${leftQueue.toList()}")
}