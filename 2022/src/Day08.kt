fun main() {

    data class Point(val x: Int, val y: Int)

    fun List<String>.parse() = this
        .filter { it.isNotBlank() }
        .flatMapIndexed { y, line -> line.mapIndexed { x, c -> Pair(Point(x, y), c.toString().toInt()) } }
        .toMap()

    // for maximum efficiency ... and fun!
    fun <R> Map<Point, Int>.getSweepMaps(block: (Int?, Int, R?) -> R): List<Map<Point, R>> {
        val maxX = this.keys.maxOf { it.x }
        val maxY = this.keys.maxOf { it.y }

        val sweeps = listOf<Triple<List<Point>, (Point) -> Point, (Point) -> Point>>(
            Triple(
                (0 .. maxY).map { y -> Point(0, y) },
                { p -> Point(p.x + 1, p.y) },
                { p -> Point(p.x - 1, p.y) },
            ),
            Triple(
                (0 .. maxY).map { y -> Point(maxX, y) },
                { p -> Point(p.x - 1, p.y) },
                { p -> Point(p.x + 1, p.y) },
            ),
            Triple(
                (0 .. maxX).map { x -> Point(x, 0) },
                { p -> Point(p.x, p.y + 1) },
                { p -> Point(p.x, p.y - 1) },
            ),
            Triple(
                (0 .. maxX).map { x -> Point(x, maxY) },
                { p -> Point(p.x, p.y - 1) },
                { p -> Point(p.x, p.y + 1) },
            ),
        )

        return sweeps.map { (initial, next, previous) ->
            generateSequence(initial) { points -> points.map { next(it) } }
                .takeWhile { points -> points.any { it in this.keys } }
                .fold(emptyMap()) { map, points ->
                    map + points.map { point ->
                        val prevValue = map[previous(point)]
                        val prevHeight = this[previous(point)]
                        val height = this[point] ?: error("shouldn't get here")
                        point to block(prevHeight, height, prevValue)
                    }
                }
        }
    }

    fun part1(input: List<String>): Int {
        val heightMap = input.parse()
        return heightMap
            .getSweepMaps { ph, _, ch: Int? -> maxOf(ch ?: -1, ph ?: -1) }
            .flatMap { it.entries }
            .groupBy({ it.key }) { it.value }
            .mapValues { (point, heights) -> heights.any { h -> h < (heightMap[point] ?: -1) } }
            .count { it.value }
    }

    fun part2(input: List<String>): Int {
        data class SweepState(
            val distance: Int,
            val distanceToEdge: Int,
            val previousHeightDistances: Map<Int, Int>,
        )

        return input
            .parse()
            .getSweepMaps { _, height, sweepState: SweepState? ->
                val distanceToEdge =  sweepState?.distanceToEdge ?: 0
                val previousHeightDistances = (sweepState?.previousHeightDistances ?: emptyMap())
                    .filter { (h, _) -> h >= height }
                    .mapValues { (_, d) -> d + 1 }
                SweepState(
                    distance = previousHeightDistances.values.minOrNull() ?: distanceToEdge,
                    distanceToEdge = distanceToEdge + 1,
                    previousHeightDistances = previousHeightDistances + Pair(height, 0)
                )
            }
            .map { sweepMap -> sweepMap.mapValues { (_, hd) -> hd.distance } }
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
