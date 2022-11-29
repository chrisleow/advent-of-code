fun main() {

    data class Point(val x: Int, val y: Int)
    data class State(val enhancements: List<Boolean>, val points: Set<Point>, val boundaryState: Boolean)

    fun <T> Iterable<T>.split(delimiter: (T) -> Boolean): List<List<T>> = this
        .fold(listOf(emptyList<T>())) { acc, item ->
            when (delimiter(item)) {
                true -> acc + listOf(emptyList())
                false -> acc.dropLast(1) + listOf(acc.last() + item)
            }
        }
        .filter { it.isNotEmpty() }

    fun parseInput(input: List<String>): State {
        val groups = input.split { it.isBlank() }.toList()
        return State(
            enhancements = groups[0]
                .joinToString("") { it.trim() }
                .map { it == '#' },
            points = groups[1]
                .flatMapIndexed { y, line ->
                    line.mapIndexedNotNull { x, char -> if (char == '#') Point(x, y) else null }
                }
                .toSet(),
            boundaryState = false,
        )
    }

    fun State.toDebugString(): String = buildString {
        val (minX, maxX) = Pair(points.minOf { it.x }, points.maxOf { it.x })
        val (minY, maxY) = Pair(points.minOf { it.y }, points.maxOf { it.y })
        (minY .. maxY).forEach { y ->
            (minX .. maxX).forEach { x ->
                append(if (Point(x, y) in points) '\u2588' else ' ')
            }
            appendLine()
        }
    }

    fun Point.getNeighbourhood() = listOf(
        Point(x - 1, y - 1), Point(x, y - 1), Point(x + 1, y - 1),
        Point(x - 1, y), Point(x, y), Point(x + 1, y),
        Point(x - 1, y + 1), Point(x, y + 1), Point(x + 1, y + 1),
    )

    fun State.next(): State {
        val (minX, maxX) = Pair(points.minOf { it.x }, points.maxOf { it.x })
        val (minY, maxY) = Pair(points.minOf { it.y }, points.maxOf { it.y })
        val checkPoints = (minX - 1 .. maxX + 1).flatMap { x ->
            (minY - 1 .. maxY + 1).map { y -> Point(x, y) }
        }

        // remember the boundary state (all light or all dark)
        fun Point.isSet() = when {
            x in (minX ..maxX) && y in (minY .. maxY) -> this in points
            else -> boundaryState
        }

        // if first enhancement is set (number 0), boundary state will constantly flip between turns
        return copy(
            boundaryState = boundaryState xor enhancements[0],
            points = checkPoints
                .filter { point ->
                    val index = point
                        .getNeighbourhood()
                        .joinToString("") { if (it.isSet()) "1" else "0" }
                        .toInt(2)
                    enhancements[index]
                }
                .toSet(),
        )
    }

    fun part1(input: List<String>): Int {
        return parseInput(input).next().next().points.size
    }

    fun part2(input: List<String>): Int {
        return generateSequence(parseInput(input)) { it.next() }.drop(50).first().points.size
    }

    // test
    val testInput = readInput("Day20_test")
    check(part1(testInput) == 35)
    check(part2(testInput) == 3351)

    val input = readInput("Day20")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")

//    run {
//        val lifeInput = readInput("Day20_life")
//        generateSequence(parseInput(lifeInput)) { it.next() }.forEach { state ->
//            println(state.toDebugString())
//            println()
//            println()
//            Thread.sleep(500L)
//        }
//    }
}
