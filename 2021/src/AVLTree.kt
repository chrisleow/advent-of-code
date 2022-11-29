import kotlin.Comparator
import kotlin.math.max

/**
 * Generic AVL Tree with left / right rotations etc...
 *
 * Purely functional data structure that allows us to implement more complex algorithms,
 * purely functionally!
 *
 * Instead of providing in-depth comments, here's more legit materials:
 *
 * https://www.programiz.com/dsa/avl-tree
 */
class AVLTree<T: Any> private constructor (private val root: Node<T>?, private val comparator: Comparator<T>) {

    private data class Limit<T>(val limit: T, val inclusive: Boolean)

    private data class Node<T>(val item: T, val left: Node<T>?, val right: Node<T>?) {
        val height: Int = max(left?.height ?: 0, right?.height ?: 0) + 1
        val size: Int = (left?.size ?: 0) + (right?.size ?: 0) + 1
    }

    private fun Node<T>.rotateLeft(): Node<T> {
        val oldMiddle = this
        val oldRight = this.right ?: error("expected a right node")
        return Node(
            item = oldRight.item,
            left = Node(oldMiddle.item, oldMiddle.left, oldRight.left),
            right = oldRight.right,
        )
    }

    private fun Node<T>.rotateRight(): Node<T> {
        val oldMiddle = this
        val oldLeft = this.left ?: error("expected a left node")
        return Node(
            item = oldLeft.item,
            left = oldLeft.left,
            right = Node(oldMiddle.item, oldLeft.right, oldMiddle.right),
        )
    }

    private fun Node<T>.rotateLeftRight(): Node<T> {
        val oldMiddle = this
        val oldLeft = this.left ?: error("expected a left node")
        val oldLeftRight = this.left.right ?: error("expected a left / right node")
        return Node(
            item = oldLeftRight.item,
            left = Node(oldLeft.item, oldLeft.left, oldLeftRight.left),
            right = Node(oldMiddle.item, oldLeftRight.right, oldMiddle.right),
        )
    }

    private fun Node<T>.rotateRightLeft(): Node<T> {
        val oldMiddle = this
        val oldRight = this.right ?: error("expected a right node")
        val oldRightLeft = this.right.left ?: error("expected a right / left node")
        return Node(
            item = oldRightLeft.item,
            left = Node(oldMiddle.item, oldMiddle.left, oldRightLeft.left),
            right = Node(oldRight.item, oldRightLeft.right, oldRight.right),
        )
    }

    private tailrec fun Node<T>.rebalance(): Node<T> {
        fun Node<T>.balanceFactor() = (left?.height ?: 0) - (right?.height ?: 0)
        return when (this.balanceFactor()) {
            in (2 .. Int.MAX_VALUE) -> {
                if ((left?.balanceFactor() ?: 0) < 0) {
                    this.rotateLeftRight().rebalance()
                } else {
                    this.rotateRight().rebalance()
                }
            }
            in (Int.MIN_VALUE .. -2) -> {
                if ((right?.balanceFactor() ?: 0) > 0) {
                    this.rotateRightLeft().rebalance()
                } else {
                    this.rotateLeft().rebalance()
                }
            }
            else -> this
        }
    }

    private fun Node<T>?.add(item: T): Node<T> {
        return when (this) {
            null -> Node(item, null, null)
            else -> when (comparator.compare(item, this.item)) {
                -1 -> this.copy(left = left.add(item)).rebalance()
                1 -> this.copy(right = right.add(item)).rebalance()
                else -> this.copy(item = item)
            }
        }
    }

    private fun Node<T>?.remove(item: T): Node<T>? {
        fun Node<T>.detachLeftmost(): Pair<T, Node<T>?> {
            return when (this.left) {
                null -> Pair(this.item, this.right)
                else -> {
                    val (leftmost, newLeft) = left.detachLeftmost()
                    return Pair(leftmost, this.copy(left = newLeft).rebalance())
                }
            }
        }
        return when (this) {
            null -> null
            else -> when (comparator.compare(item, this.item)) {
                -1 -> copy(left = left.remove(item)).rebalance()
                1 -> copy(right = right.remove(item)).rebalance()
                else -> when {
                    this.left == null && this.right == null -> null
                    this.left == null -> this.right
                    this.right == null -> this.left
                    else -> {
                        val (leftmost, newRight) = this.right.detachLeftmost()
                        this.copy(item = leftmost, right = newRight).rebalance()
                    }
                }
            }
        }
    }

    private tailrec fun Node<T>?.contains(targetItem: T): Boolean = when (this) {
        null -> false
        else -> when (comparator.compare(targetItem, item)) {
            -1 -> left.contains(targetItem)
            1 -> right.contains(targetItem)
            else -> true
        }
    }

    private tailrec fun Node<T>?.find(targetItem: T): T? = when (this) {
        null -> null
        else -> when (comparator.compare(targetItem, item)) {
            -1 -> left.find(targetItem)
            1 -> right.find(targetItem)
            else -> item
        }
    }

    // redis-inspired catch-all iteration
    private fun Node<T>?.select(from: Limit<T>?, to: Limit<T>?, reversed: Boolean): Sequence<T> {
        if (this == null) {
            return emptySequence()
        }

        val node = this
        return sequence {
            val includeLeft = if (from == null) true else comparator.compare(from.limit, node.item) < 0
            val includeRight = if (to == null) true else comparator.compare(to.limit, node.item) > 0
            val includeLeftCurrent = when {
                from == null -> true
                from.inclusive -> comparator.compare(from.limit, node.item) <= 0
                else -> comparator.compare(from.limit, node.item) < 0
            }
            val includeRightCurrent = when {
                to == null -> true
                to.inclusive -> comparator.compare(to.limit, node.item) >= 0
                else -> comparator.compare(to.limit, node.item) > 0
            }

            // recursively emit before or after (optionally reversed)
            when (reversed) {
                false -> {
                    if (includeLeft) {
                        yieldAll(node.left.select(from, to, false))
                    }
                    if (includeLeftCurrent && includeRightCurrent) {
                        yield(node.item)
                    }
                    if (includeRight) {
                        yieldAll(node.right.select(from, to, false))
                    }
                }
                true -> {
                    if (includeRight) {
                        yieldAll(node.right.select(from, to, false))
                    }
                    if (includeLeftCurrent && includeRightCurrent) {
                        yield(node.item)
                    }
                    if (includeLeft) {
                        yieldAll(node.left.select(from, to, false))
                    }
                }
            }
        }
    }

    // simple accessors
    val size: Int = root?.size ?: 0
    operator fun contains(item: T) = root.contains(item)
    fun find(item: T) = root.find(item)

    // sequence scanners
    fun scan(): Sequence<T> = root.select(null, null, false)
    fun scanFrom(from: T, inclusive: Boolean = true) = root.select(Limit(from, inclusive), null, false)
    fun scanReversed() = root.select(null, null, true)
    fun scanFromReversed(from: T, inclusive: Boolean = true) = root.select(null, Limit(from, inclusive), true)

    // modifiers
    fun add(item: T): AVLTree<T> {
        return AVLTree(root.add(item), comparator)
    }
    fun addAll(items: Iterable<T>): AVLTree<T> {
        return AVLTree(items.fold(root) { node, item -> node.add(item) }, comparator)
    }
    fun remove(item: T): AVLTree<T> {
        return AVLTree(root.remove(item), comparator)
    }
    fun removeAll(items: Iterable<T>): AVLTree<T> {
        return AVLTree(items.fold(root) { node, item -> node.remove(item) }, comparator)
    }

    fun debugPrint() {
        fun Node<T>.debugPrint(prefix: String, indent: String) {
            println("${indent}${prefix} ${this.item}")
            if (this.left != null) {
                this.left.debugPrint("Left: ","${indent}  ")
            }
            if (this.right != null) {
                this.right.debugPrint("Right:", "${indent}  ")
            }
        }
        if (root != null) {
            root.debugPrint("Root: ", "")
            println()
        }
    }

    companion object {
        operator fun <T: Comparable<T>> invoke() = AVLTree<T>(null, compareBy { it })
        operator fun <T: Any> invoke(comparator: Comparator<T>) = AVLTree(null, comparator)
    }
}


fun main() {
    val fullTree = (1 .. 100).fold(AVLTree<Int>()) { tree, x -> tree.add(x) }
    println(fullTree.scanFrom(23).toList())
    println(103 in fullTree)
    println(fullTree.find(94))
}