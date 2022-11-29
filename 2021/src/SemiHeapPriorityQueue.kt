/**
 * Implementation of priority search queue:
 *
 * https://www.cs.ox.ac.uk/ralf.hinze/publications/ICFP01.pdf
 *
 * Consists of a root with a winner and a "loser-tree" with the following conditions:
 *
 * Semi-Heap:
 *      1. Every priority in the loser tree must be greater than or equal to the priority of the root.
 *      2. For all nodes in the loser tree, the priority of the loser's binding must be less than or equal to
 *          the priorities of the bindings of the subtree from which the loser originates.  The loser
 *          originates from the left subtree if the key is less than or equal to the split key, else it
 *          originates from the right subtree.
 *
 * Search:
 *      For all nodes, the keys in the left subtree must be less than or equal to the split key, and the keys
 *      in the right subtree must be greater than the split key.
 *
 * Key:
 */

class SemiHeapPriorityQueue<T> private constructor (
    private val root: Root<T>,
    private val comparator: Comparator<T>,
) {

    private sealed class Root<T> {
        class Empty<T> : Root<T>()
        data class Winner<T>(val winner: T, val losers: Losers<T>) : Root<T>()
    }

    private sealed class Losers<T> {
        class Empty<T> : Losers<T>()
        data class Tree<T>(val split: T, val left: Losers<T>, val right: Losers<T>) : Losers<T>()
    }

    private fun Root<T>.combineOrdered(other: Root<T>): Root<T> {
        return when {
            this is Root.Empty -> other
            other is Root.Empty -> this
            this is Root.Winner && other is Root.Winner -> {
                when (comparator.compare(this.winner, other.winner)) {
                    1 -> Root.Winner(other.winner, Losers.Tree(this.winner, this.losers, other.losers))
                    else -> Root.Winner(this.winner, Losers.Tree(other.winner, this.losers, other.losers))
                }
            }
            else -> error("When is actually exhaustive, but the Kotlin compiler complains ...")
        }
    }

//    companion object {
//        fun runTest() {
//            val queue = (1 .. 10).fold(SemiHeapPriorityQueue(Root.Empty<Int>(), compareBy { it })) { acc, x ->
//                SemiHeapPriorityQueue(acc.root.combineOrdered())
//            }
//            val
//        }
//    }
}