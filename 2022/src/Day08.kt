fun main() {

    data class Point(val x: Int, val y: Int)

    fun List<String>.parse() = this
        .filter { it.isNotBlank() }
        .flatMapIndexed { y, line -> line.mapIndexed { x, c -> Pair(Point(x, y), c.toString().toInt()) } }
        .toMap()

    // for maximum efficiency ... and fun!
    fun <R> Map<Point, Int>.getSweepMaps(block: (Int, R?) -> R): List<Map<Point, R>> {
        val maxX = this.keys.maxOf { it.x }
        val maxY = this.keys.maxOf { it.y }

        val sweeps = listOf(
            Pair((0 .. maxY).map { y -> Point(0, y) }, Pair(1, 0)),
            Pair((0 .. maxY).map { y -> Point(maxX, y) }, Pair(-1, 0)),
            Pair((0 .. maxX).map { x -> Point(x, 0) }, Pair(0, 1)),
            Pair((0 .. maxX).map { x -> Point(x, maxY) }, Pair(0, -1)),
        )

        return sweeps.map { (initialPoints, deltaXY) ->
            val (deltaX, deltaY) = deltaXY
            initialPoints
                .flatMap { initialPoint ->
                    sequence {
                        var point = initialPoint
                        var previousAcc: R? = null
                        while (point in this@getSweepMaps) {
                            val height = this@getSweepMaps[point] ?: error("bad point")
                            val accumulator = block(height, previousAcc)
                            yield(Pair(point, accumulator))
                            point = Point(point.x + deltaX, point.y + deltaY)
                            previousAcc = accumulator
                        }
                    }
                }
                .toMap()
        }
    }

    fun part1(input: List<String>): Int {
        return input
            .parse()
            .getSweepMaps { height, state: Pair<Int, Boolean>? ->
                val maxHeight = state?.first ?: -1
                Pair(maxOf(height, maxHeight), height > maxHeight)
            }
            .flatMap { it.entries }
            .groupBy({ it.key }) { it.value.second }
            .mapValues { (_, visibilities) -> visibilities.any { it } }
            .count { it.value }
    }

    fun part2(input: List<String>): Int {
        data class SweepState(
            val distance: Int,
            val distanceToEdge: Int,
            val distanceByHeight: Map<Int, Int>,
        )

        return input
            .parse()
            .getSweepMaps { height, state: SweepState? ->
                val distanceToEdge = state?.distanceToEdge ?: 0
                val newDistanceByHeight = (state?.distanceByHeight ?: emptyMap())
                    .filter { (h, _) -> h >= height }
                    .mapValues { (_, d) -> d + 1 }
                SweepState(
                    distance = newDistanceByHeight.values.minOrNull() ?: distanceToEdge,
                    distanceToEdge = distanceToEdge + 1,
                    distanceByHeight = newDistanceByHeight + Pair(height, 0)
                )
            }
            .map { sweepMap -> sweepMap.mapValues { (_, state) -> state.distance } }
            .flatMap { it.entries }
            .groupBy({ it.key }) { it.value }
            .mapValues { (_, distances) -> distances.reduce { a, b -> a * b } }
            .maxOf { (_, score) -> score }
    }

    val testInput = readInput("Day08_test")
    check(part1(testInput) == 21)
    check(part2(testInput) == 8)

    val input = readInput("Day08")
    println("Part 1 ${part1(input)}")
    println("Part 2 ${part2(input)}")
}
