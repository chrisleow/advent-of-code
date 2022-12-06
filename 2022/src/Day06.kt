fun main() {

    fun List<String>.parse() = this
        .joinToString("")
        .replace(" ", "")

    fun part1(input: List<String>) = input
        .parse()
        .asSequence()
        .windowed(4)
        .takeWhile { cs -> cs.distinct().size < cs.size }
        .count()
        .let { c -> c + 4 }

    fun part2(input: List<String>) = input
        .parse()
        .asSequence()
        .windowed(14)
        .takeWhile { cs -> cs.distinct().size < cs.size }
        .count()
        .let { c -> c + 14 }

    val testInput = readInput("Day06_test")
    check(part1(testInput) == 7)
    check(part2(testInput) == 19)

    val input = readInput("Day06")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
