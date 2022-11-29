fun main() {

    data class Point(val x: Int, val y: Int)
    data class Fold(val axis: Char, val units: Int)
    data class State(val points: Set<Point>, val folds: List<Fold>)

    fun parseState(input: List<String>): State {
        val commaRegex = "(\\d+),(\\d+)".toRegex()
        val foldRegex = "fold along ([xy])=(\\d+)".toRegex()
        return input.fold(State(emptySet(), emptyList())) { state, line ->
            val commaMatch = commaRegex.matchEntire(line)
            val foldMatch = foldRegex.matchEntire(line)
            when {
                commaMatch != null -> {
                    val point = Point(commaMatch.groupValues[1].toInt(), commaMatch.groupValues[2].toInt())
                    state.copy(points = state.points + point)
                }
                foldMatch != null -> {
                    val fold = Fold(foldMatch.groupValues[1].first(), foldMatch.groupValues[2].toInt())
                    state.copy(folds = state.folds + fold)
                }
                else -> state
            }
        }
    }

    fun State.toDisplayString(): String {
        val points = this.points
        val maxX = points.maxOf { it.x }
        val maxY = points.maxOf { it.y }
        return buildString {
            (0..maxY).forEach { y ->
                (0..maxX).forEach { x ->
                    append(if (Point(x, y) in points) "\u2588" else " ")
                }
                append("\n")
            }
        }
    }

    fun State.next(): State? {
        val fold = this.folds.firstOrNull() ?: return null
        return this.copy(
            points = when (fold.axis) {
                'x' -> points.map { (x, y) -> Point(if (x < fold.units) x else (2 * fold.units) - x, y) }.toSet()
                'y' -> points.map { (x, y) -> Point(x, if (y < fold.units) y else (2 * fold.units) - y) }.toSet()
                else -> error("Shouldn't get here, can't be bothered with enum / sealed class.")
            },
            folds = folds.drop(1),
        )
    }

    fun part1(input: List<String>) = parseState(input).next()?.points?.size ?: 0
    fun part2(input: List<String>) = generateSequence(parseState(input)) { it.next() }.last().toDisplayString()

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day13_test")
    check(part1(testInput) == 17)

    val input = readInput("Day13")
    println("Part 1: ${part1(input)}")
    println("Part 2:\n${part2(input)}")
}
