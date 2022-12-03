import java.util.*
import kotlin.math.abs

fun main() {

    data class Point(val x: Int, val y: Int)
    data class Move(val char: Char, val cost: Int, val start: Point, val end: Point)
    data class State(val charMap: Map<Point, Char>, val cost: Int, val previous: State?)

    val homeChars = mapOf(3 to 'A', 5 to 'B', 7 to 'C', 9 to 'D')
    val unitCosts = mapOf('A' to 1, 'B' to 10, 'C' to 100, 'D' to 1000)

    fun List<String>.parseInput() = this
        .filter { it.isNotBlank() }
        .flatMapIndexed { y, line -> line.mapIndexed { x, char -> Point(x, y) to char } }
        .filter { (_, c) -> c != ' ' }
        .toMap()

    fun Map<Point, Char>.toDebugString(): String {
        val map = this
        val maxX = map.keys.maxOf { it.x }
        val maxY = map.keys.maxOf { it.y }
        return buildString {
            (0 .. maxY).forEach { y ->
                val line = (0 .. maxX)
                    .map { x -> map[Point(x, y)] ?: ' ' }
                    .joinToString("")
                    .trimEnd()
                appendLine(line)
            }
        }
    }

    fun Map<Point, Char>.isWinner() = this.entries
        .filter { (_, c) -> c in setOf('A', 'B', 'C', 'D') }
        .all { (p, c) -> p.y > 1 && homeChars[p.x] == c }

    fun Map<Point, Char>.getMoves(): Sequence<Move> {
        val charMap = this

        // get the destination points where possible
        val destinationPointsByChar = charMap.entries
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

        return charMap.entries
            .asSequence()
            .filter { (_, c) -> c in setOf('A', 'B', 'C', 'D') }
            .flatMap { (point, char) ->
                fun getCost(p: Point) = unitCosts[char]!! * (abs(point.x - p.x) + abs(point.y - p.y))

                // on the "home row", want to move to the destination row (if home row is clear)
                if (point.y == 1) {
                    val destination = destinationPointsByChar[char] ?: return@flatMap emptySequence()
                    val xRange = minOf(point.x, destination.x) .. maxOf(point.x, destination.x)
                    return@flatMap if (!xRange.all { x -> x == point.x || charMap[Point(x, 1)] == '.' }) {
                        emptySequence()
                    } else {
                        sequenceOf(Move(char, getCost(destination), point, destination))
                    }
                }

                // can't get to home row, no moves
                if ((1 until point.y).any { y -> this[point.copy(y = y)] != '.' }) {
                    return@flatMap emptySequence()
                }

                // walk backwards and forwards to get to home row location
                return@flatMap sequence {
                    yieldAll(
                        generateSequence(point.x) { x -> x - 1 }
                            .takeWhile { x -> charMap[Point(x, 1)] == '.' }
                            .filter { x -> x !in homeChars.keys }
                            .map { x -> Move(char, getCost(Point(x, 1)), point, Point(x, 1))}
                    )
                    yieldAll(
                        generateSequence(point.x) { x -> x + 1 }
                            .takeWhile { x -> charMap[Point(x, 1)] == '.' }
                            .filter { x -> x !in homeChars.keys }
                            .map { x -> Move(char, getCost(Point(x, 1)), point, Point(x, 1)) }
                    )
                }
            }
    }

    fun Map<Point, Char>.getCost(printWinnerTrail: Boolean): Int {
        val seenCharMaps = mutableSetOf<Map<Point, Char>>()
        val queue = PriorityQueue<State>(compareBy { it.cost })
        queue.add(State(charMap = this, cost = 0, previous = null))

        while (queue.isNotEmpty()) {
            val state = queue.remove() ?: error("Should never get here.")
            if (state.charMap in seenCharMaps) {
                continue
            }
            if (state.charMap.isWinner()) {
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
                            println(prevState.charMap.toDebugString())
                            println("  Cost: ${prevState.cost}")
                            println()
                        }
                }
                return state.cost
            }

            seenCharMaps.add(state.charMap)
            state.charMap
                .getMoves()
                .forEach { move ->
                    val newCharMapEntries = listOf(move.start to '.', move.end to move.char)
                    queue.add(
                        State(
                            charMap = state.charMap + newCharMapEntries,
                            cost = state.cost + move.cost,
                            previous = state,
                        )
                    )
                }
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
