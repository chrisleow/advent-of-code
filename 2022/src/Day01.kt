fun main() {

    fun getCalorieSets(input: List<String>) = input
        .split { it.isBlank() }
        .map { lines -> lines.map { it.toInt() } }
        .filter { it.isNotEmpty() }

    fun part1(input: List<String>) = getCalorieSets(input)
        .maxOf { it.sum() }

    fun part2(input: List<String>) = getCalorieSets(input)
        .map { it.sum() }
        .sortedDescending()
        .take(3)
        .sum()

    val testInput = readInput("Day01_test")
    check(part1(testInput) == 24000)
    check(part2(testInput) == 45000)

    val input = readInput("Day01")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
