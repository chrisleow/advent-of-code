fun main() {

    data class Point(val x: Int, val y: Int)

    fun List<String>.parse() = this
        .filter { it.isNotBlank() }
        .flatMapIndexed { y, line -> line.mapIndexed { x, c -> Pair(Point(x, y), c.toString().toInt()) } }
        .toMap()

    // for maximum efficiency ... and fun!
    fun <R> Map<Point, Int>.getSweepResults(block: (Int, R?) -> R) = sequence {
        val maxX = keys.maxOf { it.x }
        val maxY = keys.maxOf { it.y }

        val sweeps = listOf(
            Triple((0 .. maxY).map { y -> Point(0, y) }, 1, 0),
            Triple((0 .. maxY).map { y -> Point(maxX, y) }, -1, 0),
            Triple((0 .. maxX).map { x -> Point(x, 0) }, 0, 1),
            Triple((0 .. maxX).map { x -> Point(x, maxY) }, 0, -1),
        )

        sweeps.forEach { (initialPoints, deltaX, deltaY) ->
            initialPoints.forEach { initialPoint ->
                generateSequence(initialPoint) { Point(it.x + deltaX, it.y + deltaY) }
                    .takeWhile { it in this@getSweepResults }
                    .fold(null) { accumulator: R?, point ->
                        val height = this@getSweepResults[point] ?: error("bad point")
                        block(height, accumulator).also { yield(point to it) }
                    }
            }
        }
    }

    fun part1(input: List<String>): Int {
        data class State(
            val maxHeight: Int,
            val isVisible: Boolean,
        )

        return input
            .parse()
            .getSweepResults { height, state: State? ->
                val maxHeight = state?.maxHeight ?: -1
                State(maxOf(height, maxHeight), height > maxHeight)
            }
            .groupingBy { (p, _) -> p }
            .aggregate { _, visible: Boolean?, (_, state), _ -> (visible ?: false) || state.isVisible }
            .count { (_, visible) -> visible }
    }

    fun part2(input: List<String>): Int {
        data class State(
            val distance: Int,
            val distanceToEdge: Int,
            val distanceByHeight: Map<Int, Int>,
        )

        return input
            .parse()
            .getSweepResults { height, state: State? ->
                val distanceToEdge = state?.distanceToEdge?.plus(1) ?: 0
                val distanceByHeight = (state?.distanceByHeight ?: emptyMap())
                    .filter { (h, _) -> h >= height }
                    .mapValues { (_, d) -> d + 1 }
                State(
                    distance = distanceByHeight.values.minOrNull() ?: distanceToEdge,
                    distanceToEdge = distanceToEdge,
                    distanceByHeight = distanceByHeight + Pair(height, 0)
                )
            }
            .groupingBy { (p, _) -> p }
            .aggregate { _, score: Int?, (_, state), _ -> (score ?: 1) * state.distance }
            .maxOf { (_, score) -> score }
    }

    val testInput = readInput("Day08_test")
    check(part1(testInput) == 21)
    check(part2(testInput) == 8)

    val input = readInput("Day08")
    println("Part 1 ${part1(input)}")
    println("Part 2 ${part2(input)}")
}
