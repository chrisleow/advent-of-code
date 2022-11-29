fun main() {

    data class CharPair(val left: Char, val right: Char)

    fun parseInput(input: List<String>): Pair<String, Map<CharPair, Char>> {
        val regex = "(\\w)(\\w)\\s*->\\s*(\\w)".toRegex()
        val initialString = input.first().trim()
        val rules = input
            .mapNotNull { line -> regex.matchEntire(line)?.groupValues }
            .associate { gv -> CharPair(gv[1].single(), gv[2].single()) to gv[3].single() }
        return Pair(initialString, rules)
    }

    fun Map<CharPair, Long>.next(ruleMap: Map<CharPair, Char>): Map<CharPair, Long> {
        return this.entries
            .flatMap { (charPair, count) ->
                when (val insertChar = ruleMap[charPair]) {
                    null -> listOf(
                        Pair(charPair, count),
                    )
                    else -> listOf(
                        Pair(charPair.copy(right = insertChar), count),
                        Pair(charPair.copy(left = insertChar), count),
                    )
                }
            }
            .groupBy({ it.first }) { it.second }
            .mapValues { it.value.sum() }
    }

    fun getElementDifference(input: List<String>, steps: Int): Long {
        val (initialString, ruleMap) = parseInput(input)

        // convert to a map of pairs of neighbours (with counts)
        val initialMap = initialString
            .zipWithNext()
            .map { (left, right) -> CharPair(left, right) }
            .groupingBy { it }
            .eachCount()
            .mapValues { (_, count) -> count.toLong() }
        val finalMap = generateSequence(initialMap) { it.next(ruleMap) }
            .drop(steps)
            .first()

        // remember that each element is double-counted in a pair, and to correct for slight
        // under-counting at the start and end of the sequence.
        val elementCounts = finalMap
            .flatMap { (charPair, count) -> listOf(Pair(charPair.left, count), Pair(charPair.right, count)) }
            .plus(listOf(Pair(initialString.first(), 1L), Pair(initialString.last(), 1L)))
            .groupBy({ it.first }) { it.second }
            .mapValues { it.value.sum() / 2 }
        return elementCounts.maxOf { it.value } - elementCounts.minOf { it.value }
    }

    fun part1(input: List<String>): Long = getElementDifference(input, 10)
    fun part2(input: List<String>): Long = getElementDifference(input, 40)

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day14_test")
    check(part1(testInput) == 1588L)
    check(part2(testInput) == 2188189693529L)

    val input = readInput("Day14")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
