sealed class AutoCompleteResult {
    data class Result(val string: String) : AutoCompleteResult()
    data class IllegalChar(val char: Char) : AutoCompleteResult()
}

fun main() {

    fun getAutoCompleteString(line: String): AutoCompleteResult {
        val opposites = mapOf('(' to ')', '[' to ']', '{' to '}', '<' to '>')

        tailrec fun getAutoCompleteString(index: Int, closeStack: List<Char>): AutoCompleteResult = when {
            index >= line.length -> {
                val expectedCloseString = closeStack.reversed().joinToString("")
                AutoCompleteResult.Result(expectedCloseString)
            }
            line[index] in "([{<" -> {
                val expectedCloseChar = opposites[line[index]]!!
                getAutoCompleteString(index + 1, closeStack + expectedCloseChar)
            }
            else -> {
                when (val closeChar = line[index]) {
                    closeStack.last() -> getAutoCompleteString(index + 1, closeStack.dropLast(1))
                    else -> AutoCompleteResult.IllegalChar(closeChar)
                }
            }
        }

        return getAutoCompleteString(0, emptyList())
    }

    fun part1(input: List<String>): Long {
        val charScores = mapOf(')' to 3L, ']' to 57L, '}' to 1197L, '>' to 25137L)
        return input
            .filter { it.isNotBlank() }
            .sumOf { line ->
                when (val result = getAutoCompleteString(line)) {
                    is AutoCompleteResult.Result -> 0L
                    is AutoCompleteResult.IllegalChar -> charScores[result.char] ?: 0L
                }
            }
    }

    fun part2(input: List<String>): Long {
        val charScores = mapOf(')' to 1, ']' to 2, '}' to 3, '>' to 4)
        return input
            .filter { it.isNotBlank() }
            .mapNotNull { (getAutoCompleteString(it) as? AutoCompleteResult.Result)?.string }
            .map { chars ->
                chars.fold(0L) { score, char ->
                    (score * 5) + (charScores[char] ?: error("Shouldn't get here."))
                }
            }
            .let { allScores -> allScores.sorted()[allScores.size / 2] }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day10_test")
    check(part1(testInput) == 26397L)
    check(part2(testInput) == 288957L)

    val input = readInput("Day10")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
