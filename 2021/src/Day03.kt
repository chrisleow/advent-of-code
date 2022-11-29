fun main() {

    fun Iterable<String>.onesAt(index: Int) = this.count { it[index] == '1' }
    fun Iterable<String>.zeroesAt(index: Int) = this.count { it[index] == '0' }

    fun getGamma(input: List<String>): Int =
        (0 until input.maxOf { it.length })
            .joinToString("") { idx -> if (input.onesAt(idx) > input.zeroesAt(idx)) "1" else "0" }
            .toInt(2)

    fun getEpsilon(input: List<String>): Int =
        (0 until input.maxOf { it.length })
            .joinToString("") { idx -> if (input.onesAt(idx) < input.zeroesAt(idx)) "1" else "0" }
            .toInt(2)

    fun getRating(input: List<String>, getCriteriaBit: (count1: Int, count0: Int) -> Char): Int {
        tailrec fun getNumber(index: Int, numbers: List<String>): String =
            when (numbers.size) {
                0 -> error("Should never get to 0 numbers.")
                1 -> numbers.single()
                else -> {
                    val criteriaBit = getCriteriaBit(numbers.onesAt(index), numbers.zeroesAt(index))
                    getNumber(index + 1, numbers.filter { it[index] == criteriaBit })
                }
            }

        return getNumber(0, input).toInt(2)
    }

    fun part1(input: List<String>): Int {
        val cleanInput = input.filter { it.isNotBlank() }.map { it.trim() }
        val gamma = getGamma(cleanInput)
        val epsilon = getEpsilon(cleanInput)
        return gamma * epsilon
    }

    fun part2(input: List<String>): Int {
        val cleanInput = input.filter { it.isNotBlank() }.map { it.trim() }
        val oxygenRating = getRating(cleanInput) { c1, c0 -> if (c1 >= c0) '1' else '0' }
        val scrubberRating = getRating(cleanInput) { c1, c0 -> if (c1 < c0) '1' else '0' }
        return oxygenRating * scrubberRating
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 198)
    check(part2(testInput) == 230)

    val input = readInput("Day03")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
