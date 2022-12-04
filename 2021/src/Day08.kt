fun main() {

    data class InputLine(val signalPatterns: List<String>, val outputPatterns: List<String>)
    data class Digit(val value: Int, val chars: Set<Char>)

    val digits = listOf(
        Digit(0, "abcefg".toSet()),
        Digit(1, "cf".toSet()),
        Digit(2, "acdeg".toSet()),
        Digit(3, "acdfg".toSet()),
        Digit(4, "bcdf".toSet()),
        Digit(5, "abdfg".toSet()),
        Digit(6, "abdefg".toSet()),
        Digit(7, "acf".toSet()),
        Digit(8, "abcdefg".toSet()),
        Digit(9, "abcdfg".toSet()),
    )

    val digitsByLength = digits.groupBy { it.chars.count() }

    fun getInputLines(input: List<String>): List<InputLine> = input
        .filter { it.isNotBlank() && "|" in it }
        .map { line ->
            line.split("|")
                .map { section -> section.split(" ").filter { it.isNotBlank() } }
                .let { InputLine(it.dropLast(1).flatten(), it.last()) }
        }

    fun List<String>.inferMapping(): Map<Char, Char> {
        fun infer(index: Int, possibilities: Map<Char, Set<Char>>): Map<Char, Char>? {
            when {
                possibilities.values.any { it.isEmpty() } -> return null
                index >= this.size -> return possibilities.mapValues { it.value.single() }
            }

            val scrambledChars = this[index]
            return (digitsByLength[scrambledChars.length] ?: error("No possible match for '$scrambledChars'."))
                .firstNotNullOfOrNull { maybeDigit ->
                    val nextPossibilities = possibilities
                        .mapValues { (key, possibleChars) ->
                            if (key in scrambledChars) {
                                possibleChars intersect maybeDigit.chars
                            } else {
                                possibleChars - maybeDigit.chars
                            }
                        }
                    infer(index + 1, nextPossibilities)
                }
        }

        return infer(0, "abcdefg".associateWith { "abcdefg".toSet() })
            ?: error("Could not get a mapping for $this.")
    }

    fun Map<Char, Char>.applyTo(input: List<String>) = input
        .map { scrambledChars ->
            val mappedChars = scrambledChars.map { c -> this[c] }.toSet()
            digits.first { it.chars == mappedChars }.value
        }
        .fold(0) { acc, digit -> (acc * 10) + digit }

    fun part1(input: List<String>) = getInputLines(input)
        .map { inputLine ->
            inputLine.signalPatterns
                .inferMapping()
                .applyTo(inputLine.outputPatterns)
                .toString()
        }
        .sumOf { output -> output.count { it in "1478" } }

    fun part2(input: List<String>) = getInputLines(input)
        .sumOf { inputLine ->
            inputLine.signalPatterns
                .inferMapping()
                .applyTo(inputLine.outputPatterns)
        }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 26)
    check(part2(testInput) == 61229)

    val input = readInput("Day08")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
