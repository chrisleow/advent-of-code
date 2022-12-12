import java.util.PriorityQueue

fun main() {

    data class Point(val x: Int, val y: Int)
    data class Step(val point: Point, val count: Int, val previous: Step?)

    fun List<String>.parse() = this
        .filter { it.isNotBlank() }
        .flatMapIndexed { y, line -> line.mapIndexed { x, char -> Point(x, y) to char } }
        .toMap()

    fun getElevation(map: Map<Point, Char>, point: Point) = when(val char = map[point]) {
        null -> Int.MAX_VALUE
        'S' -> 1
        'E' -> 26
        else -> char.code - 'a'.code + 1
    }

    fun Step.getNextSteps(map: Map<Point, Char>, predicate: (Int) -> Boolean) = sequence {
        val elevation = getElevation(map, point)
        val adjacentPoints = listOf(
            Point(point.x - 1, point.y),
            Point(point.x + 1, point.y),
            Point(point.x, point.y - 1),
            Point(point.x, point.y + 1),
        )
        adjacentPoints
            .filter { p -> predicate(getElevation(map, p) - elevation) }
            .forEach { p -> yield(Step(p, count + 1, this@getNextSteps)) }
    }

    fun part1(input: List<String>): Int {
        val map = input.parse()
        val start = map.filter { (_, c) -> c == 'S' }.map { (p, _) -> p }.first()
        val end = map.filter { (_, c) -> c == 'E' }.map { (p, _) -> p }.first()

        val seenPoints = mutableSetOf<Point>()
        val queue = PriorityQueue<Step>(compareBy { it.count })
        queue.add(Step(start, 0, null))

        while (queue.isNotEmpty()) {
            val currentStep = queue.remove() ?: break
            if (currentStep.point == end) {
                return currentStep.count
            }

            if (currentStep.point in seenPoints) {
                continue
            } else {
                seenPoints.add(currentStep.point)
            }

            currentStep
                .getNextSteps(map) { deltaElevation -> deltaElevation <= 1 }
                .forEach { queue.add(it) }
        }

        return -1
    }

    fun part2(input: List<String>): Int {
        val map = input.parse()
        val end = map.filter { (_, c) -> c == 'E' }.map { (p, _) -> p }.first()

        val seenPoints = mutableSetOf<Point>()
        val queue = PriorityQueue<Step>(compareBy { it.count })
        queue.add(Step(end, 0, null))

        while (queue.isNotEmpty()) {
            val currentStep = queue.remove() ?: break
            if (getElevation(map, currentStep.point) == 1) {
                return currentStep.count
            }

            if (currentStep.point in seenPoints) {
                continue
            } else {
                seenPoints.add(currentStep.point)
            }

            currentStep
                .getNextSteps(map) { deltaElevation -> deltaElevation >= -1 }
                .forEach { queue.add(it) }
        }

        return -1
    }

    val testInput = readInput("Day12_test")
    check(part1(testInput) == 31)
    check(part2(testInput) == 29)

    val input = readInput("Day12")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
