fun main() {

    data class InputLine(val signalPatterns: List<String>, val outputPatterns: List<String>)
    data class Digit(val value: Int, val pattern: Set<Char>)

    val digits = listOf(
        Digit(0, "ABCEFG".toSet()),
        Digit(1, "CF".toSet()),
        Digit(2, "ACDEG".toSet()),
        Digit(3, "ACDFG".toSet()),
        Digit(4, "BCDF".toSet()),
        Digit(5, "ABDFG".toSet()),
        Digit(6, "ABDEFG".toSet()),
        Digit(7, "ACF".toSet()),
        Digit(8, "ABCDEFG".toSet()),
        Digit(9, "ABCDFG".toSet()),
    )

    fun parseInput(input: List<String>): List<InputLine> = input
        .filter { it.isNotBlank() && "|" in it }
        .map { line ->
            line.split("|")
                .map { section -> section.split(" ").filter { it.isNotBlank() } }
                .let { InputLine(it.dropLast(1).flatten(), it.last()) }
        }

    fun decodeOutput(line: InputLine): Int {
        val scrambledPatterns = (line.signalPatterns + line.outputPatterns).distinct().sortedBy { it.length }
        val digitPatternsByLength = digits.map { it.pattern }.groupBy { it.size }

        // boil down set of possibilities assuming a digit pattern to an output pattern
        fun Map<Char, Set<Char>>.restrict(scrambledChars: Set<Char>, realChars: Set<Char>) =
            this.mapValues { (scrambledChar, existingRealChars) ->
                when (scrambledChar in scrambledChars) {
                    true -> existingRealChars intersect realChars
                    false -> existingRealChars - realChars
                }
            }

        // search recursively by testing hypotheses for each digit (and restricting the mapping
        // of possibilities as we go)
        fun search(possibilities: Map<Char, Set<Char>>, index: Int): Map<Char, Char>? = when {
            possibilities.any { it.value.isEmpty() } -> null
            index >= scrambledPatterns.size -> possibilities.mapValues { it.value.single() }
            else -> {
                val scrambledPattern = scrambledPatterns[index].toSet()
                digitPatternsByLength[scrambledPattern.size]?.firstNotNullOfOrNull {
                    search(possibilities.restrict(scrambledPattern, it), index + 1)
                }
            }
        }

        fun unscramble(pattern: String, mapping: Map<Char, Char>): Int {
            val realPattern = pattern.mapNotNull { mapping[it] }.toSet()
            return digits.first { it.pattern == realPattern }.value
        }

        // start search at the beginning with all possibilities
        val mapping = search("abcdefg".associateWith { "ABCDEFG".toSet() }, 0)
            ?: error("Should have a final mapping")
        return line.outputPatterns
            .map { pattern -> unscramble(pattern, mapping) }
            .fold(0) { acc, digit -> (acc * 10) + digit }
    }

    fun part1(input: List<String>): Int {
        val lengths = digits
            .filter { it.value in listOf(1, 4, 7, 8) }
            .map { it.pattern.size }
        return parseInput(input)
            .flatMap { it.outputPatterns }
            .count { it.length in lengths }
    }

    fun part2(input: List<String>): Int {
        return parseInput(input).sumOf { decodeOutput(it) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 26)
    check(part2(testInput) == 61229)

    val input = readInput("Day08")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
