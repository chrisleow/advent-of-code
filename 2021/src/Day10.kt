fun main() {

    val bracketsRegex = listOf("()", "[]" ,"{}", "<>")
        .joinToString("|") { Regex.escape(it) }
        .toRegex()

    tailrec fun collapse(line: String): String =
        when (val newLine = bracketsRegex.replace(line, "")) {
            line -> line
            else -> collapse(newLine)
        }

    fun part1(input: List<String>): Long {
        val charScores = mapOf(')' to 3L, ']' to 57L, '}' to 1197L, '>' to 25137L)
        return input
            .asSequence()
            .filter { line -> line.isNotBlank() }
            .map { line -> collapse(line) }
            .mapNotNull { collapsedLine -> collapsedLine.firstOrNull { c -> c in ")]}>" } }
            .sumOf { illegalChar -> charScores[illegalChar] ?: 0L }
    }

    fun part2(input: List<String>): Long {
        val charScores = mapOf('(' to 1, '[' to 2, '{' to 3, '<' to 4)
        val allScores = input
            .asSequence()
            .filter { line -> line.isNotBlank() }
            .map { line -> collapse(line) }
            .filter { collapsedLine -> ")]}>".all { c -> c !in collapsedLine } }
            .map { collapsedLine -> collapsedLine.reversed() }
            .map { revLine -> revLine.fold(0L) { sc, c -> (sc * 5) + (charScores[c] ?: 0) } }
            .sorted()
            .toList()
        return allScores[allScores.size / 2]
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day10_test")
    check(part1(testInput) == 26397L)
    check(part2(testInput) == 288957L)

    val input = readInput("Day10")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
