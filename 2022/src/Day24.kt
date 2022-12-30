fun main() {

    data class Point(val x: Int, val y: Int)
    data class Blizzard(val point: Point, val direction: Char)
    data class Blizzards(val list: List<Blizzard>, val maxX: Int, val maxY: Int) {
        val byPoint = list.groupBy { it.point }
    }

    data class StateAtTime(val time: Int, val points: Set<Point>, val blizzards: Blizzards)

    fun List<String>.parse(): Blizzards {
        val blizzards = this
            .filter { it.isNotBlank() }
            .flatMapIndexed { y, line ->
                line.mapIndexedNotNull { x, char ->
                    if (char in "^v<>") Blizzard(Point(x, y), char) else null
                }
            }
        val allPoints = this
            .filter { it.isNotBlank() }
            .flatMapIndexed { y, line ->
                line.mapIndexedNotNull { x, char ->
                    if (!char.isWhitespace()) Point(x, y) else null
                }
            }

        return Blizzards(blizzards, allPoints.maxOf { it.x }, allPoints.maxOf { it.y })
    }

    fun Blizzards.next() = copy(
        list = list.map { blizzard ->
            val nextPoint = when (blizzard.direction) {
                '>' -> blizzard.point.copy(x = if (blizzard.point.x == maxX - 1) 1 else blizzard.point.x + 1)
                '<' -> blizzard.point.copy(x = if (blizzard.point.x == 1) maxX - 1 else blizzard.point.x - 1)
                'v' -> blizzard.point.copy(y = if (blizzard.point.y == maxY - 1) 1 else blizzard.point.y + 1)
                '^' -> blizzard.point.copy(y = if (blizzard.point.y == 1) maxY - 1 else blizzard.point.y - 1)
                else -> error("shouldn't get here.")
            }
            Blizzard(nextPoint, blizzard.direction)
        }
    )

    fun StateAtTime.next(): StateAtTime {
        val nextBlizzards = blizzards.next()
        val entrance = Point(1, 0)
        val exit = Point(blizzards.maxX - 1, blizzards.maxY)

        return StateAtTime(
            time = time + 1,
            blizzards = nextBlizzards,
            points = points
                .asSequence()
                .flatMap { point ->
                    listOf(
                        point,
                        point.copy(x = point.x - 1),
                        point.copy(x = point.x + 1),
                        point.copy(y = point.y - 1),
                        point.copy(y = point.y + 1),
                    )
                }
                .filter { point ->
                    when {
                        point == entrance || point == exit -> true
                        point.x !in 1 until nextBlizzards.maxX -> false
                        point.y !in 1 until nextBlizzards.maxY -> false
                        point in nextBlizzards.byPoint.keys -> false
                        else -> true
                    }
                }
                .toSet(),
        )
    }

    fun part1(input: List<String>): Int {
        val initialState = StateAtTime(0, setOf(Point(1, 0)), input.parse())

        val exit = Point(initialState.blizzards.maxX - 1, initialState.blizzards.maxY)
        return generateSequence(initialState) { it.next() }
            .first { state -> state.points.any { it == exit } }
            .time
    }

    fun part2(input: List<String>): Int {
        val state0 = StateAtTime(0, setOf(Point(1, 0)), input.parse())

        val entrance = Point(1, 0)
        val exit = Point(state0.blizzards.maxX - 1, state0.blizzards.maxY)

        // initial trip
        val state0End = generateSequence(state0) { it.next() }
            .first { state -> state.points.any { it == exit } }

        // snacks
        val state1 = state0End.copy(points = setOf(exit))
        val state1End = generateSequence(state1) { it.next() }
            .first { state -> state.points.any { it == entrance } }

        // return after snacks
        val state2 = state1End.copy(points = setOf(entrance))
        return generateSequence(state2) { it.next() }
            .first { state -> state.points.any { it == exit } }
            .time
    }

    val testInput = readInput("Day24_test")
    check(part1(testInput) == 18)
    check(part2(testInput) == 54)

    val input = readInput("Day24")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
