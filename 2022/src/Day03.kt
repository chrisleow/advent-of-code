fun main() {

    fun Char.priority(): Int = when (this) {
        in ('a' .. 'z') -> this.code - 'a'.code + 1
        in ('A' .. 'Z') -> this.code - 'A'.code + 27
        else -> error("Invalid character.")
    }

    fun part1(input: List<String>) = input
        .asSequence()
        .filter { it.isNotBlank() }
        .map { it.trim() }
        .map { Pair(it.substring(0, it.length / 2), it.substring(it.length / 2)) }
        .map { (left, right) -> (left.toSet() intersect right.toSet()).single() }
        .sumOf { it.priority() }

    fun part2(input: List<String>) = input
        .asSequence()
        .filter { it.isNotBlank() }
        .chunked(3)
        .map { lines ->
            lines
                .flatMap { it.asIterable() }
                .distinct()
                .single { c -> lines.all { c in it } }
        }
        .sumOf { it.priority() }

    val testInput = readInput("Day03_test")
    check(part1(testInput) == 157)
    check(part2(testInput) == 70)

    val input = readInput("Day03")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
