fun main() {

    data class Point(val x: Int, val y: Int)
    data class State(val points: Set<Point>, val directions: List<Pair<List<String>, String>>)

    fun List<String>.parse() = this
        .filter { it.isNotBlank() }
        .flatMapIndexed { y, line ->
            line.mapIndexedNotNull { x, char -> if (char == '#') Point(x, y) else null }
        }
        .toSet()

    fun State.next(): State {
        val proposals = points.associateWith { point ->
            val neighbours = mapOf(
                "N" to Point(x = point.x, y = point.y - 1),
                "NE" to Point(x = point.x + 1, y = point.y - 1),
                "NW" to Point(x = point.x - 1, y = point.y - 1),
                "E" to Point(x = point.x + 1, y = point.y),
                "W" to Point(x = point.x - 1, y = point.y),
                "S" to Point(x = point.x, y = point.y + 1),
                "SE" to Point(x = point.x + 1, y = point.y + 1),
                "SW" to Point(x = point.x - 1, y = point.y + 1),
            )

            val occupied = neighbours.mapValues { (_, p) -> p in points }
            if (occupied.values.all { isOccupied -> !isOccupied }) {
                return@associateWith null
            }

            directions.firstNotNullOfOrNull { (checkDirections, moveDirection) ->
                if (checkDirections.all { direction -> occupied[direction] != true }) {
                    neighbours[moveDirection]
                } else {
                    null
                }
            }
        }
        return State(
            points = proposals
                .map { (original, proposal) ->
                    when {
                        proposal == null -> original
                        proposals.values.count { it == proposal } >= 2 -> original
                        else -> proposal
                    }
                }
                .toSet(),
            directions = directions.drop(1) + directions.first(),
        )
    }

    fun Set<Point>.generateStates(): Sequence<State> {
        val initialDirections = listOf(
            listOf("N", "NE", "NW") to "N",
            listOf("S", "SE", "SW") to "S",
            listOf("W", "NW", "SW") to "W",
            listOf("E", "NE", "SE") to "E",
        )
        return generateSequence(State(this, initialDirections)) { it.next() }
    }

    fun part1(input: List<String>): Int {
        val finalState = input.parse()
            .generateStates()
            .drop(10)
            .first()

        val (minX, maxX) = finalState.points.minOf { it.x } to finalState.points.maxOf { it.x }
        val (minY, maxY) = finalState.points.minOf { it.y } to finalState.points.maxOf { it.y }
        return (minX .. maxX).sumOf { x ->
            (minY .. maxY).sumOf { y ->
                (if (Point(x, y) in finalState.points) 0 else 1) as Int
            }
        }
    }

    fun part2(input: List<String>): Int {
        return input.parse()
            .generateStates()
            .windowed(2)
            .takeWhile { states -> states[0].points != states[1].points }
            .count() + 1
    }

    val testInput = readInput("Day23_test")
    check(part1(testInput) == 110)
    check(part2(testInput) == 20)

    val input = readInput("Day23")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
