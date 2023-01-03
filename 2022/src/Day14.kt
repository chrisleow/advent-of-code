fun main() {

    data class Point(
        val x: Int,
        val y: Int,
    )
    data class State(
        val minX: Int,
        val maxX: Int,
        val minY: Int,
        val maxY: Int,
        val restedSand: Set<Point>,
        val currentSand: Point,
        val horizontalSegments: Map<Int, List<IntRange>>,
        val verticalSegments: Map<Int, List<IntRange>>,
    )

    fun List<String>.parse(): State {
        val segmentRegex = "(\\d+),(\\d+)".toRegex()
        val segments = this.flatMap { line ->
            segmentRegex
                .findAll(line)
                .map { m -> Point(m.groupValues[1].toInt(), m.groupValues[2].toInt()) }
                .windowed(2)
        }

        fun biRange(a1: Int, a2: Int) = if (a1 < a2) a1 .. a2 else a2 .. a1

        return State(
            minX = segments.flatMap { (p1, p2) -> listOf(p1.x, p2.x) }.min(),
            maxX = segments.flatMap { (p1, p2) -> listOf(p1.x, p2.x) }.max(),
            minY = segments.flatMap { (p1, p2) -> listOf(p1.y, p2.y) }.min(),
            maxY = segments.flatMap { (p1, p2) -> listOf(p1.y, p2.y) }.max(),
            restedSand = emptySet(),
            currentSand = Point(500, 0),
            horizontalSegments = segments
                .filter { (p1, p2) -> p1.y == p2.y }
                .map { (p1, p2) -> p1.y to biRange(p1.x, p2.x) }
                .groupBy({ (y, _) -> y }) { (_, range) -> range },
            verticalSegments = segments
                .filter { (p1, p2) -> p1.x == p2.x }
                .map { (p1, p2) -> p1.x to biRange(p1.y, p2.y) }
                .groupBy({ (x, _) -> x }) { (_, range) -> range },
        )
    }

    fun State.isBlocked(point: Point) =
        (point in restedSand) ||
                (horizontalSegments[point.y]?.any { point.x in it } == true) ||
                (verticalSegments[point.x]?.any { point.y in it } == true) ||
                (point.y >= maxY + 2)

    fun State.next(): State {
        val trySands = listOf(
            currentSand.copy(y = currentSand.y + 1),
            currentSand.copy(x = currentSand.x - 1, y = currentSand.y + 1),
            currentSand.copy(x = currentSand.x + 1, y = currentSand.y + 1)
        )
        for (trySand in trySands) {
            if (!isBlocked(trySand)) {
                return copy(currentSand = trySand)
            }
        }

        // blocked on all sides, sand comes to rest
        return copy(restedSand = restedSand + currentSand, currentSand = Point(500, 0))
    }

    fun part1(input: List<String>): Int {
        val finalState = generateSequence(input.parse()) { it.next() }
            .dropWhile { it.currentSand.y < it.maxY }
            .first()
        return finalState.restedSand.size
    }

    fun part2(input: List<String>): Int {
        val finalState = generateSequence(input.parse()) { it.next() }
            .dropWhile { Point(500, 0) !in it.restedSand }
            .first()
        return finalState.restedSand.size
    }

    val testInput = readInput("Day14_test")
    check(part1(testInput) == 24)
    check(part2(testInput) == 93)

    val input = readInput("Day14")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
