fun main() {

    data class Point(val x: Int, val y: Int)
    data class RecursivePoint(val x: Int, val y: Int, val depth: Int)

    fun List<String>.parse() = this
        .filter { it.isNotBlank() }
        .flatMapIndexed { y, line ->
            line.mapIndexedNotNull { x, c -> if (c == '#') Point(x, y) else null }
        }
        .toSet()

    fun Point.getAdjacent() = sequenceOf(
        Point(x, y - 1),
        Point(x - 1, y),
        Point(x, y + 1),
        Point(x + 1, y),
    )

    fun RecursivePoint.getAdjacent() = sequence {
        yield(RecursivePoint(x - 1, y, depth))
        yield(RecursivePoint(x + 1, y, depth))
        yield(RecursivePoint(x, y - 1, depth))
        yield(RecursivePoint(x, y + 1, depth))

        // outer edges
        if (x == 0) yield(RecursivePoint(1, 2, depth - 1))
        if (x == 4) yield(RecursivePoint(3, 2, depth - 1))
        if (y == 0) yield(RecursivePoint(2, 1, depth - 1))
        if (y == 4) yield(RecursivePoint(2, 3, depth - 1))

        // inner edges
        if (x == 1 && y == 2) yieldAll((0 until 5).map { RecursivePoint(0, it, depth + 1) })
        if (x == 3 && y == 2) yieldAll((0 until 5).map { RecursivePoint(4, it, depth + 1) })
        if (x == 2 && y == 1) yieldAll((0 until 5).map { RecursivePoint(it, 0, depth + 1) })
        if (x == 2 && y == 3) yieldAll((0 until 5).map { RecursivePoint(it, 4, depth + 1) })
    }

    fun <V, U> Iterable<V>.crossWith(other: Iterable<U>) =
        flatMap { first -> other.map { second -> Pair(first, second) } }

    fun Set<Point>.getRating() =
        sumOf { (x, y) -> 1 shl (x + (5 * y)) }

    fun Set<Point>.next() =  (0 until 5)
        .crossWith(0 until 5)
        .map { (x, y) -> Point(x, y) }
        .filter { point ->
            val adjacentCount = point.getAdjacent().count { it in this }
            if (point in this) {
                adjacentCount == 1
            } else {
                adjacentCount in (1 .. 2)
            }
        }
        .toSet()

    fun Set<RecursivePoint>.next() = (0 until 5)
        .crossWith(0 until 5)
        .map { (x, y) -> Point(x, y) }
        .filter { point -> point != Point(2, 2) }
        .crossWith((minOf { it.depth } - 1) .. (maxOf { it.depth } + 1))
        .map { (point, depth) -> RecursivePoint(point.x, point.y, depth) }
        .filter { recursivePoint ->
            val adjacentCount = recursivePoint.getAdjacent().count { it in this }
            if (recursivePoint in this) {
                adjacentCount == 1
            } else {
                adjacentCount in (1 .. 2)
            }
        }
        .toSet()

    fun part1(input: List<String>): Int {
        val initialState = Pair(input.parse(), emptySet<Set<Point>>())
        return generateSequence(initialState) { (s, ss) -> Pair(s.next(), ss + setOf(s)) }
            .windowed(2)
            .filter { pairs -> pairs.map { it.second.size }.distinct().count() == 1 }
            .map { pairs -> pairs.first().first.getRating() }
            .first()
    }

    fun part2(input: List<String>, limit: Int): Int {
        val initialState = input
            .parse()
            .map { RecursivePoint(it.x, it.y, 0) }
            .toSet()
        return generateSequence(initialState) { it.next() }
            .drop(limit)
            .first()
            .count()
    }

    val testInput = readInput("Day_2019_24_test")
    check(part1(testInput) == 2129920)
    check(part2(testInput, 10) == 99)

    val input = readInput("Day_2019_24")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input, 200)}")
}