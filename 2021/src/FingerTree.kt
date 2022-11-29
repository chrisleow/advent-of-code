interface Measure<V, T> {
    val empty: V
    fun combine(a: V, b: V): V
    operator fun invoke(item: T): V
}

fun main() {
    FingerTree.runTest()
}

/**
 * Monoid-Enhanced Finger Tres
 *
 * https://andrew.gibiansky.com/blog/haskell/finger-trees/
 */
class FingerTree<V: Any, T: Any> private constructor(private val root: Tree<V, T>) : Iterable<T> {

    /**
     * Public methods
     */

    override fun iterator(): Iterator<T> = root.toSequence().iterator()

    /**
     * Private Implementation
     */

    sealed class Tree<V : Any, T : Any> {
        abstract val measure: Measure<V, T>

        data class Empty<V : Any, T : Any>(
            override val measure: Measure<V, T>) : Tree<V, T>()
        data class Single<V : Any, T : Any>(
            val value: T,
            override val measure: Measure<V, T>) : Tree<V, T>()
        data class Deep<V : Any, T : Any>(
            val annotation: V,
            val prefix: Digit<T>,
            val deeper: Tree<V, Branch<V, T>>,
            val suffix: Digit<T>,
            override val measure: Measure<V, T>) : Tree<V, T>()
    }

    sealed class Branch<V : Any, T : Any> {
        abstract val annotation: V

        data class Branch2<V : Any, T : Any>(
            override val annotation: V,
            val first: T,
            val second: T) : Branch<V, T>()
        data class Branch3<V : Any, T : Any>(
            override val annotation: V,
            val first: T,
            val second: T,
            val third: T) : Branch<V, T>()
    }

    sealed class Digit<T : Any> {
        data class One<T : Any>(val first: T) : Digit<T>()
        data class Two<T : Any>(val first: T, val second: T) : Digit<T>()
        data class Three<T : Any>(val first: T, val second: T, val third: T) : Digit<T>()
        data class Four<T : Any>(val first: T, val second: T, val third: T, val fourth: T) : Digit<T>()
    }

    // powers iterators
    private fun <T : Any> Tree<*, T>.toSequence(): Sequence<T> = when (this) {
        is Tree.Empty<*, T> -> emptySequence()
        is Tree.Single<*, T> -> sequenceOf(value)
        is Tree.Deep<*, T> -> sequence {
            yieldAll(prefix.toList())
            yieldAll(deeper.toSequence().flatMap { it.toList() })
            yieldAll(suffix.toList())
        }
    }

    // helper functions for digits
    private fun <T : Any> Digit<T>.toList(): List<T> = when (this) {
        is Digit.One<T> -> listOf(first)
        is Digit.Two<T> -> listOf(first, second)
        is Digit.Three<T> -> listOf(first, second, third)
        is Digit.Four<T> -> listOf(first, second, third, fourth)
    }

    private fun <T : Any> List<T>.toDigit(): Digit<T> = when (this.size) {
        0 -> error("cannot transform an empty list to a digit")
        1 -> Digit.One(this[0])
        2 -> Digit.Two(this[0], this[1])
        3 -> Digit.Three(this[0], this[1], this[2])
        4 -> Digit.Four(this[0], this[1], this[2], this[3])
        else -> error("cannot transform a list of size ${this.size} into a digit.")
    }

    private fun <V : Any, T : Any> Branch<V, T>.toList(): List<T> = when (this) {
        is Branch.Branch2 -> listOf(first, second)
        is Branch.Branch3 -> listOf(first, second, third)
    }

    private fun <V : Any, T : Any> List<T>.toBranch(measure: Measure<V, T>): Branch<V, T> = when (this.size) {
        2 -> Branch.Branch2(measure(this), this[0], this[1])
        3 -> Branch.Branch3(measure(this), this[0], this[1], this[2])
        else -> error("cannot transform a list of size '${this.size}' to a branch.")
    }

    // helper function to convert any number of items (>= 2) into branches, globs everything into 2's, possibly
    // into 3's at the end
    private fun <V : Any, T : Any> List<T>.toBranches(measure: Measure<V, T>): List<Branch<V, T>> {
        tailrec fun consume(index: Int, buffer: List<T>, branches: List<Branch<V, T>>): List<Branch<V, T>> {
            return when {
                index >= this.size - 1 -> {
                    branches + buffer.toBranch(measure)
                }
                else -> when (buffer.size) {
                    3 -> {
                        val newBuffer= buffer.drop(2)
                        val newBranch = buffer.take(2).toBranch(measure)
                        consume(index + 1, newBuffer + this[index], branches + newBranch)
                    }
                    else -> {
                        consume(index + 1, buffer + this[index], branches)
                    }
                }
            }
        }
        return consume(0, emptyList(), emptyList())
    }

    // helper functions to deal with concatenating lists of monoids and measuring things
    private operator fun <V : Any, T : Any> Measure<V, T>.invoke(vararg items: T) = items
        .map { this(it) }
        .reduce { a, b -> combine(a, b) }
    private operator fun <V : Any, T : Any> Measure<V, T>.invoke(items: Iterable<T>) = items
        .map { this(it) }
        .reduce { a, b -> combine(a, b) }
    private operator fun <V : Any, T : Any> Measure<V, T>.invoke(digit: Digit<T>) = digit
        .toList()
        .map { this(it) }
        .reduce { a, b -> combine(a, b) }
    private operator fun <V : Any, T : Any> Measure<V, T>.invoke(branch: Branch<V, T>) = branch
        .toList()
        .map { this(it) }
        .reduce { a, b -> combine(a, b) }
    private operator fun <V : Any, T : Any> Measure<V, T>.invoke(tree: Tree<V, T>) =
        tree.getAnnotation()

    // lift measure to branch
    private fun <V : Any, T : Any> Measure<V, T>.toBranch(): Measure<V, Branch<V, T>> {
        val original = this
        return object : Measure<V, Branch<V, T>> {
            override val empty = original.empty
            override fun combine(a: V, b: V) = original.combine(a, b)
            override operator fun invoke(item: Branch<V, T>) = item.annotation
        }
    }

    // measure a tree
    private fun <V : Any, T : Any> Tree<V, T>.getAnnotation(): V = when (this) {
        is Tree.Empty<V, T> -> measure.empty
        is Tree.Single<V, T> -> measure(value)
        is Tree.Deep<V, T> -> annotation
    }

    // helper functions to rebuild a tree (with annotations) after replacing something
    private fun <V : Any, T : Any> Tree.Deep<V, T>.replace(
        prefix: List<T>? = null,
        deeper: Tree<V, Branch<V, T>>? = null,
        suffix: List<T>? = null,
    ): Tree.Deep<V, T> {
        return Tree.Deep(
            annotation = measure.combine(
                (deeper ?: this.deeper).getAnnotation(),
                measure((prefix ?: this.prefix.toList()) + (suffix ?: this.suffix.toList())),
            ),
            prefix = prefix?.toDigit() ?: this.prefix,
            deeper = deeper ?: this.deeper,
            suffix= suffix?.toDigit() ?: this.suffix,
            measure = measure,
        )
    }

    // helper function to create a tree from a list of items
    private fun <V : Any, T : Any> createTree(
        prefix: List<T>,
        deeper: Tree<V, Branch<V, T>>,
        suffix: List<T>,
        measure: Measure<V, T>
    ): Tree<V, T> {
         return when {
             prefix.isEmpty() && suffix.isEmpty() -> when (val result = deeper.viewLeft()) {
                 is ViewResult.Nil -> Tree.Empty(measure)
                 is ViewResult.View -> createTree(result.item.toList(), result.remainder, emptyList(), measure)
             }
             suffix.isEmpty() -> when (val result = deeper.viewRight()) {
                 is ViewResult.Nil -> prefix.toTree(measure)
                 is ViewResult.View -> createTree(prefix, result.remainder, result.item.toList(), measure)
             }
             prefix.isEmpty() -> when (val result = deeper.viewLeft()) {
                 is ViewResult.Nil -> suffix.toTree(measure)
                 is ViewResult.View -> createTree(result.item.toList(), result.remainder, suffix, measure)
             }
             else -> Tree.Deep(
                 annotation = measure.combine(
                     measure.combine(measure(prefix), measure(suffix)), deeper.getAnnotation()),
                 prefix = prefix.toDigit(),
                 deeper = deeper,
                 suffix = suffix.toDigit(),
                 measure = measure,
             )
        }
    }

    private fun <V : Any, T : Any> List<T>.toTree(measure: Measure<V, T>): Tree<V, T> =
        when (this.size) {
            0 -> Tree.Empty(measure)
            1 -> Tree.Single(this[0], measure)
            else -> {
                val (left, right) = when (this.size) {
                    2 -> Pair(Digit.One(this[0]), Digit.One(this[1]))
                    3 -> Pair(Digit.One(this[0]), Digit.Two(this[1], this[2]))
                    4 -> Pair(Digit.Two(this[0], this[1]), Digit.Two(this[2], this[3]))
                    else -> error("can't create digits for this number of elements")
                }
                Tree.Deep(measure(this), left, Tree.Empty(measure.toBranch()), right, measure)
            }
        }

    // in simple cases can add to existing digts etc..., must create a new node for middle tree if we
    // run out of space
    fun <V : Any, T : Any> Tree<V, T>.prepend(item: T): Tree<V, T> = when (this) {
        is Tree.Empty<V, T> -> listOf(item).toTree(measure)
        is Tree.Single<V, T> -> listOf(item, value).toTree(measure)
        is Tree.Deep<V, T> -> {
            when (prefix) {
                is Digit.One<T> -> replace(prefix = listOf(item, prefix.first))
                is Digit.Two<T> -> replace(prefix = listOf(item, prefix.first, prefix.second))
                is Digit.Three<T> -> replace(prefix = listOf(item, prefix.first, prefix.second, prefix.third))
                is Digit.Four<T> -> replace(
                    prefix = listOf(item, prefix.first),
                    deeper = deeper.prepend(prefix.toList().drop(1).toBranch(measure)),
                )
            }
        }
    }

    // in simple cases can add to existing digts etc..., must create a new node for middle tree if we
    // run out of space
    fun <V: Any, T : Any> Tree<V, T>.append(item: T): Tree<V, T> = when (this) {
        is Tree.Empty<V, T> -> listOf(item).toTree(measure)
        is Tree.Single<V, T> -> listOf(value, item).toTree(measure)
        is Tree.Deep<V, T> -> {
            when (suffix) {
                is Digit.One<T> -> replace(suffix = listOf(suffix.first, item))
                is Digit.Two<T> -> replace(suffix = listOf(suffix.first, suffix.second, item))
                is Digit.Three<T> -> replace(suffix = listOf(suffix.first, suffix.second, suffix.third, item))
                is Digit.Four<T> -> replace(
                    suffix = listOf(suffix.fourth, item),
                    deeper = deeper.append(suffix.toList().dropLast(1).toBranch(measure)),
                )
            }
        }
    }

    sealed class ViewResult<V : Any, T : Any> {
        class Nil<V : Any, T : Any> : ViewResult<V, T>()
        data class View<V : Any, T : Any>(val item: T, val remainder: Tree<V, T>) : ViewResult<V, T>()
    }

    // detach the leftmost item from the tree (if any)
    private fun <V: Any, T : Any> Tree<V, T>.viewLeft(): ViewResult<V, T> {
        return when (this) {
            is Tree.Empty<V, T> -> ViewResult.Nil()
            is Tree.Single<V, T> -> ViewResult.View(value, Tree.Empty(measure))
            is Tree.Deep<V, T> -> when (prefix) {
                is Digit.Four<T> -> ViewResult.View(prefix.first,
                    replace(prefix = listOf(prefix.second, prefix.third, prefix.fourth)))
                is Digit.Three<T> -> ViewResult.View(prefix.first,
                    replace(prefix = listOf(prefix.second, prefix.third)))
                is Digit.Two<T> -> ViewResult.View(prefix.first,
                    replace(prefix = listOf(prefix.second)))
                is Digit.One<T> -> {

                    // run out of nodes at this level, extract from branch at lower level, careful when no nodes
                    when (val result = deeper.viewLeft()) {
                        is ViewResult.Nil -> ViewResult.View(prefix.first,
                            suffix.toList().toTree(measure))
                        is ViewResult.View -> ViewResult.View(prefix.first,
                            replace(prefix = result.item.toList(), deeper = result.remainder))
                    }
                }
            }
        }
    }

    // detach the rightmost item from the tree (if any)
    private fun <V: Any, T : Any> Tree<V, T>.viewRight(): ViewResult<V, T> {
        return when (this) {
            is Tree.Empty<V, T> -> ViewResult.Nil()
            is Tree.Single<V, T> -> ViewResult.View(value, Tree.Empty(measure))
            is Tree.Deep<V, T> -> when (suffix) {
                is Digit.Four<T> -> ViewResult.View(suffix.fourth,
                    replace(suffix = listOf(suffix.first, suffix.second, suffix.third)))
                is Digit.Three<T> -> ViewResult.View(suffix.third,
                    replace(suffix = listOf(suffix.first, suffix.second)))
                is Digit.Two<T> -> ViewResult.View(suffix.second,
                    replace(suffix = listOf(suffix.first)))
                is Digit.One<T> -> {

                    // run out of nodes at this level, extract from branch at lower level, careful when no nodes
                    when (val result = deeper.viewRight()) {
                        is ViewResult.Nil -> ViewResult.View(suffix.first,
                            prefix.toList().toTree(measure))
                        is ViewResult.View -> ViewResult.View(suffix.first,
                            replace(suffix = result.item.toList(), deeper = result.remainder))
                    }
                }
            }
        }
    }

    // concatenates two trees with random stuff in the middle (makes sure measures are the same first)
    // deals with easy cases (Single & Empty), and then globs up the tail / items to be pushed down into the
    // rest of the tree
    fun <V : Any, T : Any> concatWithMiddle(prefix: Tree<V, T>, items: List<T>, suffix: Tree<V, T>): Tree<V, T> {
        assert(prefix.measure == suffix.measure) { "Measures need to be the same to concat" }
        val measure = prefix.measure
        return when {
            prefix is Tree.Empty<V, T> -> when {
                items.isEmpty() -> suffix
                else -> concatWithMiddle(Tree.Empty(measure), items.drop(1), suffix).prepend(items.first())
            }
            prefix is Tree.Single<V, T> -> {
                concatWithMiddle(Tree.Empty(measure), items, suffix).prepend(prefix.value)
            }
            suffix is Tree.Empty<V, T> -> when {
                items.isEmpty() -> prefix
                else -> concatWithMiddle(prefix, items.dropLast(1), Tree.Empty(measure)).append(items.last())
            }
            suffix is Tree.Single<V, T> -> {
                concatWithMiddle(prefix, items, Tree.Empty(measure)).append(suffix.value)
            }
            prefix is Tree.Deep<V, T> && suffix is Tree.Deep<V, T> -> prefix.replace(
                prefix = prefix.prefix.toList(),
                deeper = concatWithMiddle(
                    prefix = prefix.deeper,
                    items = (prefix.suffix.toList() + items + suffix.prefix.toList()).toBranches(measure),
                    suffix = suffix.deeper,
                ),
                suffix = suffix.suffix.toList(),
            )
            else -> error("above is actually exhaustive, but need exception for IntelliSense, shouldn't get here")
        }
    }

    // handy overload for concatenating two trees without explicitly mentioning items in the middle
    fun <V : Any, T : Any> concat(left: Tree<V, T>, right: Tree<V, T>) =
        concatWithMiddle(left, emptyList(), right)

    /**
     * Tree Splitting with annotations
     */

    private sealed class SplitResult<V : Any, T : Any> {
        class None<V : Any, T : Any>
            : SplitResult<V, T>()
        data class Split<V : Any, T : Any>(val before: Tree<V, T>, val pivot: T, val after: Tree<V, T>)
            : SplitResult<V, T>()
    }

    private fun <V : Any, T : Any> Tree<V, T>.split(predicate: (V) -> Boolean, pivotMeasure: V): SplitResult<V, T> {
        fun splitDeep(tree: Tree.Deep<V, T>): SplitResult<V, T> {

            // check the prefix
            val prefixList = tree.prefix.toList()
            val prefixPivotMeasure = prefixList.indices.fold(pivotMeasure) { pivotMeasureSoFar, index ->
                val newPivotMeasureSoFar = measure.combine(pivotMeasureSoFar, measure(prefixList[index]))
                if (predicate(newPivotMeasureSoFar)) {
                    return SplitResult.Split(
                        before = prefixList.take(index).toTree(measure),
                        pivot = prefixList[index],
                        after = createTree(prefixList.drop(index), tree.deeper, tree.suffix.toList(), measure),
                    )
                } else {
                    newPivotMeasureSoFar
                }
            }

            // check the middle of the tree
            val deeperPivotMeasure = measure.combine(prefixPivotMeasure, tree.deeper.getAnnotation())
            if (predicate(deeperPivotMeasure)) {
                when (val innerSplit = tree.deeper.split(predicate, prefixPivotMeasure)) {
                    is SplitResult.None -> return SplitResult.None()
                    is SplitResult.Split -> {
                        val innerList = innerSplit.pivot.toList()
                        val startMeasure = measure.combine(prefixPivotMeasure, innerSplit.before.getAnnotation())
                        innerList.foldIndexed(startMeasure) { index, pivotMeasureSoFar, item ->
                            val newPivotMeasureSoFar = measure.combine(pivotMeasureSoFar, measure(item))
                            if (predicate(newPivotMeasureSoFar)) {
                                return SplitResult.Split(
                                    before = createTree(
                                        prefix = tree.prefix.toList(),
                                        deeper = innerSplit.before,
                                        suffix = innerList.take(index),
                                        measure = measure,
                                    ),
                                    pivot = item,
                                    after = createTree(
                                        prefix = innerList.drop(index + 1),
                                        deeper = innerSplit.after,
                                        suffix = tree.suffix.toList(),
                                        measure = measure,
                                    ),
                                )
                            } else {
                                newPivotMeasureSoFar
                            }
                        }
                    }
                }
            }

            // check the suffix
            val suffixList = tree.suffix.toList()
            suffixList.indices.fold(deeperPivotMeasure) { pivotMeasureSoFar, index ->
                val newPivotMeasureSoFar = measure.combine(pivotMeasureSoFar, measure(suffixList[index]))
                if (predicate(newPivotMeasureSoFar)) {
                    return SplitResult.Split(
                        before = createTree(tree.prefix.toList(), tree.deeper, suffixList.take(index), measure),
                        pivot = suffixList[index],
                        after = suffixList.drop(index + 1).toTree(measure),
                    )
                } else {
                    newPivotMeasureSoFar
                }
            }

            // if we get here, it means the pivot doesn't add up
            return SplitResult.None()
        }

        // deal with all cases
        return when (this) {
            is Tree.Empty<V, T> -> SplitResult.None()
            is Tree.Deep<V, T> -> splitDeep(this)
            is Tree.Single<V, T> -> when (predicate(measure.combine(pivotMeasure, measure(value)))) {
                true -> SplitResult.Split(Tree.Empty(measure), value, Tree.Empty(measure))
                false -> SplitResult.None()
            }
        }
    }

    private fun <V : Any, T : Any> Tree<V, T>.split(predicate: (V) -> Boolean) =
        this.split(predicate, measure.empty)

    fun <V : Any, T : Any> Tree<V, T>.debugText(indent: String = ""): String {
        val tree = this

        fun Any.debugText(): String = when (this) {
            is Digit<*> -> this
                .toList()
                .joinToString(prefix = "(", separator = ", ", postfix = ")") { it.debugText() }
            is Branch<*, *> -> this
                .toList()
                .joinToString(prefix = "(", separator = ", ", postfix = ")") { it.debugText() }
            else -> this.toString()
        }

        return buildString {
            when (tree) {
                is Tree.Empty<V, T> -> appendLine("${indent}Empty")
                is Tree.Single<V, T> -> appendLine("${indent}Single: ${tree.value.debugText()}")
                is Tree.Deep<V, T> -> {
                    appendLine("${indent}Prefix: ${tree.prefix.debugText()}")
                    appendLine("${indent}Deeper:")
                    append(tree.deeper.debugText("$indent  "))
                    appendLine("${indent}Suffix: ${tree.suffix.debugText()}")
                }
            }
        }
    }

    fun runTest() {
        val measure = object : Measure<Int, Int> {
            override val empty: Int = 0
            override fun combine(a: Int, b: Int) = a + b
            override fun invoke(item: Int): Int = 1
        }
        val emptyTree: Tree<Int, Int> = Tree.Empty(measure)
        val tree = (1 .. 500).fold(emptyTree) { tree, x -> tree.append(x) }
        val split = tree.split { it > 250 } as SplitResult.Split
        println(split.before.debugText())
        println(split.pivot)
        println(split.after.debugText())
    }

    companion object {
        operator fun <V: Any, T: Any> invoke(measure: Measure<V, T>) = FingerTree(Tree.Empty(measure))

        fun runTest() {
            val measure = object : Measure<Int, Int> {
                override val empty: Int = 0
                override fun combine(a: Int, b: Int) = a + b
                override fun invoke(item: Int): Int = 1
            }
            FingerTree(measure).runTest()
        }
    }
}
