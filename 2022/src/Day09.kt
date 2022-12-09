import kotlin.math.abs

fun main() {

    data class Point(val x: Int, val y: Int)
    data class Instruction(val direction: Char, val distance: Int)

    val instructionRegex = "([LRUD])\\s+(\\d+)".toRegex()

    fun List<String>.parse() = this
        .mapNotNull { line -> instructionRegex.matchEntire(line)?.groupValues }
        .map { gv -> Instruction(gv[1].first(), gv[2].toInt()) }

    fun List<Instruction>.generateRopes(initialRope: List<Point>) = sequence {
        yield(initialRope)
        this@generateRopes
            .asSequence()
            .flatMap { instruction -> List(instruction.distance) { instruction.direction } }
            .fold(initialRope) { currentRope, direction ->
                currentRope
                    .fold(emptyList<Point>()) { partialNewRope, point ->
                        partialNewRope + when (val penultimate = partialNewRope.lastOrNull()) {
                            null -> {
                                when (direction) {
                                    'L' -> Point(point.x - 1, point.y)
                                    'R' -> Point(point.x + 1, point.y)
                                    'U' -> Point(point.x, point.y - 1)
                                    'D' -> Point(point.x, point.y + 1)
                                    else -> error("bad direction char ${direction}.")
                                }
                            }
                            else -> {
                                val deltaX = penultimate.x - point.x
                                val deltaY = penultimate.y - point.y
                                if (abs(deltaX) > 1 || abs(deltaY) > 1) {
                                    Point(
                                        x = point.x + minOf(maxOf(deltaX, -1), 1),
                                        y = point.y + minOf(maxOf(deltaY, -1), 1),
                                    )
                                } else {
                                    point
                                }
                            }
                        }
                    }
                    .also { newRope -> yield(newRope) }
            }
    }

    fun part1(input: List<String>) = input
        .parse()
        .generateRopes(listOf(Point(0, 0), Point(0, 0)))
        .map { it.last() }
        .distinct()
        .count()

    fun part2(input: List<String>) = input
        .parse()
        .generateRopes(List(10) { Point(0, 0) })
        .map { it.last() }
        .distinct()
        .count()

    val testInput = readInput("Day09_test")
    check(part1(testInput) == 88)
    check(part2(testInput) == 36)

    val input = readInput("Day09")
    println("Part 1 ${part1(input)}")
    println("Part 2 ${part2(input)}")
}
