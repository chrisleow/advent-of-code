fun main() {

    data class Point(val x: Int, val y: Int, val z: Int)

    fun List<String>.parse(): Set<Point> {
        val pointRegex = "(\\d+),(\\d+),(\\d+)".toRegex()
        return this
            .mapNotNull { line -> pointRegex.matchEntire(line)?.groupValues }
            .map { gv -> Point(gv[1].toInt(), gv[2].toInt(), gv[3].toInt()) }
            .toSet()
    }

    fun Point.getNeighbours() = listOf(
        copy(x = x - 1),
        copy(x = x + 1),
        copy(y = y - 1),
        copy(y = y + 1),
        copy(z = z - 1),
        copy(z = z + 1),
    )

    fun getSurrounding(points: Set<Point>): Set<Point> {
        val xRange = (points.minOf { it.x } - 1 .. points.maxOf { it.x } + 1)
        val yRange = (points.minOf { it.y } - 1 .. points.maxOf { it.y } + 1)
        val zRange = (points.minOf { it.z } - 1 .. points.maxOf { it.z } + 1)

        tailrec fun fill(surrounding: Set<Point>, edge: Set<Point>): Set<Point> {
            return if (edge.isEmpty()) {
                surrounding
            } else {
                val newEdge = edge
                    .asSequence()
                    .flatMap { it.getNeighbours() }
                    .filter { it.x in xRange && it.y in yRange && it.z in zRange && it !in points }
                    .filter { it !in points && it !in edge && it !in surrounding }
                    .toSet()
                fill(surrounding + edge, newEdge)
            }
        }

        return fill(emptySet(), setOf(Point(xRange.first, yRange.first, zRange.first)))
    }

    fun part1(input: List<String>): Int {
        val points = input.parse()
        return points
            .flatMap { p -> p.getNeighbours().map { p2 -> (p to p2) } }
            .count { (_, p2) -> p2 !in points }
    }

    fun part2(input: List<String>): Int {
        val points = input.parse()
        val surrounding = getSurrounding(points)
        return points
            .flatMap { p -> p.getNeighbours().map { p2 -> (p to p2) } }
            .count { (_, p2) -> p2 in surrounding }
    }

    val testInput = readInput("Day18_test")
    check(part1(testInput) == 64)
    check(part2(testInput) == 58)

    val input = readInput("Day18")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
