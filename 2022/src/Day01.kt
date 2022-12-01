fun main() {

    fun getCalorieSets(input: List<String>): List<List<Int>> {
        return input
            .fold(listOf(emptyList<Int>())) { lines, line ->
                if (line.isBlank()) {
                    lines + listOf(emptyList())
                } else {
                    lines.dropLast(1) + listOf(lines.last() + line.toInt())
                }
            }
            .filter { it.isNotEmpty() }
    }

    fun part1(input: List<String>): Int {
        return getCalorieSets(input)
            .maxOf { it.sum() }
    }

    fun part2(input: List<String>): Int {
        return getCalorieSets(input)
            .map { it.sum() }
            .sortedDescending()
            .take(3)
            .sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 24000)
    check(part2(testInput) == 45000)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}
