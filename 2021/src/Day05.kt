import kotlin.math.abs

fun main() {
    data class Point(val x: Int, val y: Int)
    data class Line(val from: Point, val to: Point)

    fun parseInput(input: List<String>): List<Line> {
        val pattern = "(\\d+),(\\d+)\\s*->\\s*(\\d+),(\\d+)".toRegex()
        return input
            .mapNotNull { pattern.matchEntire(it)?.groupValues }
            .map { gv -> Line(Point(gv[1].toInt(), gv[2].toInt()), Point(gv[3].toInt(), gv[4].toInt())) }
    }

    fun Line.interpolate(): Sequence<Point> {
        val deltaX = this.to.x - this.from.x
        val deltaY = this.to.y - this.from.y
        assert(abs(deltaX) == abs(deltaY) || deltaX == 0 || deltaY == 0) {
            "Expected non-slanting diagonal!"
        }

        // interpolate along "single-translation" deltas
        val dx = if (deltaX != 0) deltaX / abs(deltaX) else 0
        val dy = if (deltaY != 0) deltaY / abs(deltaY) else 0
        return generateSequence(this.from) {
            if (it != this.to) Point(it.x + dx, it.y + dy) else null
        }
    }

    fun part1(input: List<String>): Int {
        val pointCounts = parseInput(input)
            .asSequence()
            .filter { it.from.x == it.to.x || it.from.y == it.to.y }
            .flatMap { it.interpolate() }
            .groupingBy { it }
            .eachCount()
        return pointCounts.count { (_, count) -> count > 1 }
    }

    fun part2(input: List<String>): Int {
        val pointCounts = parseInput(input)
            .asSequence()
            .flatMap { it.interpolate() }
            .groupingBy { it }
            .eachCount()
        return pointCounts.count { (_, count) -> count > 1 }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day05_test")
    check(part1(testInput) == 5)
    check(part2(testInput) == 12)

    val input = readInput("Day05")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
