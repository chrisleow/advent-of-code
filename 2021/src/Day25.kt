fun main() {

    data class Point(val x: Int, val y: Int)
    data class State(val maxX: Int, val maxY: Int, val eastPoints: Set<Point>, val southPoints: Set<Point>, val steps: Int)

    fun parseInput(input: List<String>): State {
        val pointChars = input
            .filter { it.isNotBlank() }
            .flatMapIndexed { y, line -> line.trim().mapIndexed { x, char -> Point(x, y) to char } }
        return State(
            maxX = pointChars.maxOf { it.first.x },
            maxY = pointChars.maxOf { it.first.y },
            eastPoints = pointChars.filter { (_, char) -> char == '>' }.map { it.first }.toSet(),
            southPoints = pointChars.filter { (_, char) -> char == 'v' }.map { it.first }.toSet(),
            steps = 0,
        )
    }

    fun State.toDebugString() = buildString {
        (0 .. maxY).forEach { y ->
            (0 .. maxX).forEach { x ->
                when (Point(x, y)) {
                    in eastPoints -> append('>')
                    in southPoints -> append('v')
                    else -> append('.')
                }
            }
            appendLine()
        }
        appendLine("Step $steps")
        appendLine()
    }

    fun State.next(): State {
        val newEastPoints = eastPoints
            .map { point ->
                when (val nextPoint = Point((point.x + 1) % (maxX + 1), point.y)) {
                    in eastPoints -> point
                    in southPoints -> point
                    else -> nextPoint
                }
            }
            .toSet()
        val newSouthPoints = southPoints
            .map { point ->
                when (val nextPoint = Point(point.x, (point.y + 1) % (maxY + 1))) {
                    in newEastPoints -> point
                    in southPoints -> point
                    else -> nextPoint
                }
            }
            .toSet()
        return copy(eastPoints = newEastPoints, southPoints = newSouthPoints, steps = steps + 1)
    }

    fun part1(input: List<String>): Int {
        return generateSequence(parseInput(input)) { it.next() }
            .zipWithNext()
            .first { (a, b) -> a.eastPoints == b.eastPoints && a.southPoints == b.southPoints }
            .let { (a, b) -> maxOf(a.steps, b.steps) }
    }

    // test
    val testInput = readInput("Day25_test")
    check(part1(testInput) == 58)

    val input = readInput("Day25")
    println("Part 1: ${part1(input)}")
}
