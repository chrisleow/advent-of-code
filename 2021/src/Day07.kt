import kotlin.math.abs

fun main() {

    fun parseInput(input: List<String>): List<Int> {
        val regex = "\\d+".toRegex()
        return input.flatMap { line -> regex.findAll(line).map { it.value.toInt()} }
    }

    /**
     * Now using fully immutable, functional data structures!
     *
     * Note that this is longer than the non-functional version, and a bit more awkward too.  It would
     * help if we had a memoization monad or similar, but it's still instructive to try without.
     */
    fun getMinimumCost(lower: Int, upper: Int, calculateCost: (Int) -> Int): Int {

        // helper functions to modify map and retrieve cost
        operator fun AVLMap<Int, Int>.invoke(position: Int) =
            get(position) ?: error("position not populated")
        fun AVLMap<Int, Int>.ensurePopulated(vararg positions: Int): AVLMap<Int, Int> =
            addAll(positions.filter { !containsKey(it) }.map { it to calculateCost(it) })

        tailrec fun findCost(left: Int, right: Int, costs: AVLMap<Int, Int>): Int = when {
            right - left <= 1 -> {
                val nextCosts = costs.ensurePopulated(left, right)
                minOf(nextCosts(left), nextCosts(right))
            }
            else -> {
                val middle = (left + right) / 2
                val nextCosts = costs.ensurePopulated(middle, middle + 1)
                when {
                    nextCosts(middle) < nextCosts(middle + 1) -> findCost(left, middle, nextCosts)
                    nextCosts(middle) > nextCosts(middle + 1) -> findCost(middle + 1, right, nextCosts)
                    else -> error("I'm not built for this!")
                }
            }
        }

        return findCost(lower, upper, AVLMap())
    }

    // let's go for maximum efficiency :)
    fun getMinimumCost_NonFunctional(lower: Int, upper: Int, calculateCost: (Int) -> Int): Int {
        val memoizedCosts = mutableMapOf<Int, Int>()
        fun cost(pos: Int) = memoizedCosts.getOrPut(pos) { calculateCost(pos) }

        // this is a convex function, looking for minimum can be done with binary search
        // assessing "slope" at (middle, middle + 1)
        tailrec fun search(left: Int, right: Int): Int = when {
            right - left <= 1 -> {
                if (cost(left) <= cost(right)) left else right
            }
            else -> {
                val middle = (left + right) / 2
                when {
                    cost(middle) < cost(middle + 1) -> search(left, middle)
                    cost(middle) > cost(middle + 1) -> search(middle + 1, right)
                    else -> error("I'm not built for this!")
                }
            }
        }

        return cost(search(lower, upper))
    }

    fun part1(input: List<String>): Int {
        val positions = parseInput(input)
        val minPos = positions.minOrNull() ?: error("Empty List")
        val maxPos = positions.maxOrNull() ?: error("Empty List")
        return getMinimumCost(minPos, maxPos) { pos ->
            positions.sumOf { abs(it - pos) }
        }
    }

    fun part2(input: List<String>): Int {
        val positions = parseInput(input)
        val minPos = positions.minOrNull() ?: error("Empty List")
        val maxPos = positions.maxOrNull() ?: error("Empty List")
        return getMinimumCost(minPos, maxPos) { pos ->
            positions.sumOf {
                val delta = abs(it - pos)
                delta * (delta + 1) / 2
            }
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 37)
    check(part2(testInput) == 168)

    val input = readInput("Day07")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
