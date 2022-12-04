import java.util.*
import kotlin.math.abs

fun main() {

    data class Point(val x: Int, val y: Int)
    data class State(val charMap: Map<Point, Char>, val cost: Int, val heuristicCost: Int, val previous: State?)

    val homeChars = mapOf(3 to 'A', 5 to 'B', 7 to 'C', 9 to 'D')
    val homeXs = mapOf('A' to 3, 'B' to 5, 'C' to 7, 'D' to 9)
    val unitCosts = mapOf('A' to 1, 'B' to 10, 'C' to 100, 'D' to 1000)

    fun List<String>.parseInput() = this
        .filter { it.isNotBlank() }
        .flatMapIndexed { y, line -> line.mapIndexed { x, char -> Point(x, y) to char } }
        .filter { (_, c) -> c != ' ' }
        .toMap()

    fun State.toDebugString(): String {
        val state = this
        val maxX = state.charMap.keys.maxOf { it.x }
        val maxY = state.charMap.keys.maxOf { it.y }
        return buildString {
            (0 .. maxY).forEach { y ->
                val line = (0 .. maxX)
                    .map { x -> state.charMap[Point(x, y)] ?: ' ' }
                    .joinToString("")
                    .trimEnd()
                appendLine(line)
            }
            appendLine("Cost: ${state.cost} [Heuristic Cost: ${state.heuristicCost}]")
        }
    }

    fun Pair<Int, Int>.distanceOutsideRange(other: Int) = minOf(
        if (other in first..second) 0 else Int.MAX_VALUE,
        if (other in second .. first) 0 else Int.MAX_VALUE,
        abs(other - first),
        abs(other - second),
    )

    fun State.isWinner() = this.charMap.entries
        .filter { (_, c) -> c in setOf('A', 'B', 'C', 'D') }
        .all { (p, c) -> p.y > 1 && homeChars[p.x] == c }

    fun State.move(source: Point, target: Point): State {
        val distance = abs(source.x - target.x) + abs(source.y - target.y)
        val char = this.charMap[source] ?: error("can only move an amphipod.")

        // heuristic cost is only the extra distance we DON'T absolutely need to travel if
        // every amphipod follows its ideal trajectory
        val extraDistance = Pair(source.x, homeXs[char]!!).distanceOutsideRange(target.x)

        // update state
        return State(
            charMap = this.charMap + listOf(source to '.', target to char),
            cost = this.cost + (unitCosts[char]!! * distance),
            heuristicCost = this.heuristicCost + (unitCosts[char]!! * extraDistance),
            previous = this,
        )
    }

    fun State.getMoves(): Sequence<State> {
        val state = this

        // get the destination points where possible
        val destinationPointsByChar = state.charMap.entries
            .groupBy { (p, _) -> p.x }
            .filter { (x, _) -> x in homeChars.keys }
            .mapNotNull { (x, pointCharEntries) ->
                val homeChar = homeChars[x] ?: return@mapNotNull null
                if (pointCharEntries.all { (_, c) -> c == '#' || c == '.' || c == homeChar }) {
                    val maxEmptyY = pointCharEntries
                        .filter { (_, c) -> c == '.' }
                        .maxOf { (p, _) -> p.y }
                    Pair(homeChar, Point(x, maxEmptyY))
                } else {
                    null
                }
            }
            .toMap()

        return this.charMap.entries
            .asSequence()
            .filter { (_, c) -> c in setOf('A', 'B', 'C', 'D') }
            .flatMap { (point, char) ->

                // on the "home row", want to move to the destination row (if home row is clear)
                if (point.y == 1) {
                    val destination = destinationPointsByChar[char] ?: return@flatMap emptySequence()
                    val xRange = minOf(point.x, destination.x) .. maxOf(point.x, destination.x)
                    return@flatMap if (!xRange.all { x -> x == point.x || charMap[Point(x, 1)] == '.' }) {
                        emptySequence()
                    } else {
                        sequenceOf(state.move(point, destination))
                    }
                }

                // can't get to home row, no moves for this pod out of home row
                if ((1 until point.y).any { y -> state.charMap[point.copy(y = y)] != '.' }) {
                    return@flatMap emptySequence()
                }

                // walk backwards and forwards to get to home row location
                return@flatMap sequence {
                    yieldAll(
                        generateSequence(point.x) { x -> x - 1 }
                            .takeWhile { x -> charMap[Point(x, 1)] == '.' }
                            .filter { x -> x !in homeChars.keys }
                            .map { x -> state.move(point, Point(x, 1)) }
                    )
                    yieldAll(
                        generateSequence(point.x) { x -> x + 1 }
                            .takeWhile { x -> charMap[Point(x, 1)] == '.' }
                            .filter { x -> x !in homeChars.keys }
                            .map { x -> state.move(point, Point(x, 1)) }
                    )
                }
            }
    }

    fun Map<Point, Char>.getCost(printWinnerTrail: Boolean): Int {
        val seenCharMaps = mutableSetOf<Map<Point, Char>>()
        val queue = PriorityQueue<State>(compareBy { it.heuristicCost })
        queue.add(State(charMap = this, cost = 0, heuristicCost = 0, previous = null))

        while (queue.isNotEmpty()) {
            val state = queue.remove() ?: error("Should never get here.")
            if (state.isWinner()) {
                if (printWinnerTrail) {
                    val allPreviousStates = sequence {
                        var current: State? = state
                        while (current != null) {
                            yield(current)
                            current = current.previous
                        }
                    }

                    allPreviousStates
                        .toList()
                        .reversed()
                        .forEach { prevState ->
                            println(prevState.toDebugString())
                            println("  Cost: ${prevState.cost}")
                            println()
                        }
                }
                return state.cost
            }

            if (state.charMap in seenCharMaps) {
                continue
            }

            seenCharMaps.add(state.charMap)
            state.getMoves().forEach { queue.add(it) }
        }

        error("Completely run out of moves, shouldn't get here.")
    }

    fun part1(input: List<String>, printTrail: Boolean) = input
        .parseInput()
        .getCost(printTrail)

    val part2ExtraLines = listOf(
        "  #D#C#B#A#",
        "  #D#B#A#C#",
    )

    fun part2(input: List<String>, printTrail: Boolean) = input
        .let { ls -> ls.slice(0 .. 2) + part2ExtraLines + ls.slice(3 .. 4) }
        .parseInput()
        .getCost(printTrail)

    // test
    val testInput = readInput("Day23_test")
    check(part1(testInput, false) == 12521)
    println("Checked Part 1 ... ok.")
    check(part2(testInput, false) == 44169)
    println("Checked Part 2 ... ok.")

    val input = readInput("Day23")
    println("Part 1: ${part1(input, true)}")
    println()
    println("Part 2: ${part2(input, true)}")
}
