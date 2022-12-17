import kotlin.math.abs

fun main() {

    data class Point(val x: Int, val y: Int)

    fun List<String>.parse(): List<Pair<Point, Point>> {
        val regex = "Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)".toRegex()
        return this
            .mapNotNull { line -> regex.matchEntire(line)?.groupValues }
            .map { gv -> Pair(Point(gv[1].toInt(), gv[2].toInt()), Point(gv[3].toInt(), gv[4].toInt())) }
    }

    fun Point.distanceTo(other: Point) = abs(x - other.x) + abs(y - other.y)

    fun getCrossingPoint(line1: Pair<Point, Point>, line2: Pair<Point, Point>): Point? {
        /*

        Diagonal lines for point distances can be expressed either as:
            x + y = c      or
            x - y = c

        Crossing lines have different forms, and so if:
            x + y = c1     and
            x - y = c2

        then:

            x = (c1 + c2) / 2
            y = (c1 - c2) / y

        */

        val c1 = run {
            val line1c1 = line1.first.x + line1.first.y
            val line1c2 = line1.second.x + line1.second.y
            val line2c1 = line2.first.x + line2.first.y
            val line2c2 = line2.second.x + line2.second.y
            when {
                line1c1 == line1c2 -> line1c1
                line2c1 == line2c2 -> line2c1
                else -> return null
            }
        }

        val c2 = run {
            val line1c1 = line1.first.x - line1.first.y
            val line1c2 = line1.second.x - line1.second.y
            val line2c1 = line2.first.x - line2.first.y
            val line2c2 = line2.second.x - line2.second.y
            when {
                line1c1 == line1c2 -> line1c1
                line2c1 == line2c2 -> line2c1
                else -> return null
            }
        }

        return Point((c1 + c2) / 2, (c1 - c2) / 2)
    }

    fun Point.getBoundaryLines(distance: Int): List<Pair<Point, Point>> {
        val up = copy(y = y - distance)
        val down = copy(y = y + distance)
        val left = copy(x = x - distance)
        val right = copy(x = x + distance)
        return listOf(up to left, up to right, down to left, down to right)
    }

    fun part1(input: List<String>, y: Int): Int {
        val scannerBeacons = input.parse()
        val exclusionRanges = scannerBeacons
            .map { (scanner, beacon) ->
                val extraDistance = scanner.distanceTo(beacon) - abs(y - scanner.y)
                (scanner.x - extraDistance .. scanner.x + extraDistance)
            }
            .filter { range -> !range.isEmpty() }

        val starts = exclusionRanges
            .map { range -> range.first }
            .filter { x -> !exclusionRanges.any { x - 1 in it  } }
        val stops = exclusionRanges
            .map { range -> range.last }
            .filter { x -> !exclusionRanges.any { x + 1 in it } }
        val ranges = starts
            .map { x -> x .. stops.filter { it > x }.min() }

        val beaconCount = scannerBeacons
            .filter { (_, b) -> b.y == y }
            .map { (_, b) -> b.x }
            .distinct()
            .count()
        return ranges.sumOf { it.last - it.first + 1 } - beaconCount
    }

    fun part2(input: List<String>, maxX: Int, maxY: Int): Long {
        val scannerDistances = input
            .parse()
            .associate { (scanner, beacon) -> scanner to scanner.distanceTo(beacon) }

        val linesOfInterest = scannerDistances.entries
            .flatMap { (scanner, distance) -> scanner.getBoundaryLines(distance + 1) }

        val pointsOfInterest = linesOfInterest
            .flatMap { line1 -> linesOfInterest.mapNotNull { line2 -> getCrossingPoint(line1, line2) } }
            .filter { (x, y) -> x in (0 .. maxX) && y in (0 .. maxY) }
            .distinct()

        return pointsOfInterest
            .filter { point -> scannerDistances.all { (scanner, distance) -> scanner.distanceTo(point) > distance } }
            .map { (x, y) -> (x * 4000000L) + y }
            .single()
    }

    val testInput = readInput("Day15_test")
    check(part1(testInput, 10) == 26)
    check(part2(testInput, 20, 20) == 56000011L)

    val input = readInput("Day15")
    println("Part 1: ${part1(input, 2000000)}")
    println("Part 2: ${part2(input, 4000000, 4000000)}")
}
