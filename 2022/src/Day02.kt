fun main() {

    val lineRegex = "([ABC])\\s+([XYZ])".toRegex()

    fun List<String>.parseAsInput() = this
        .filter { it.isNotBlank() }
        .mapNotNull { lineRegex.matchEntire(it) }
        .map { match -> Pair(match.groupValues[1].first(), match.groupValues[2].first()) }

    fun part1(input: List<String>) = input
        .parseAsInput()
        .sumOf { (firstChar, secondChar) ->
            val opponentScore = when (firstChar) {
                'A' -> 1
                'B' -> 2
                'C' -> 3
                else -> error("Shouldn't get here.")
            }
            val myScore = when(secondChar) {
                'X' -> 1
                'Y' -> 2
                'Z' -> 3
                else -> error("Shouldn't get here.")
            }
            val winnerScore = when((myScore - opponentScore + 3) % 3) {
                0 -> 3
                1 -> 6
                2 -> 0
                else -> error("Shouldn't get here.")
            }
            winnerScore + myScore
        }

    fun part2(input: List<String>) = input
        .parseAsInput()
        .sumOf { (firstChar, secondChar) ->
            val opponentScore = when (firstChar) {
                'A' -> 1
                'B' -> 2
                'C' -> 3
                else -> error("Shouldn't get here.")
            }
            val (winnerScore, myScore) = when(secondChar) {
                'X' -> Pair(0, ((opponentScore + 1) % 3) + 1)
                'Y' -> Pair(3, opponentScore)
                'Z' -> Pair(6, (opponentScore % 3) + 1)
                else -> error("Shouldn't get here.")
            }
            winnerScore + myScore
        }

    val testInput = readInput("Day02_test")
    check(part1(testInput) == 15)
    check(part2(testInput) == 12)

    val input = readInput("Day02")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
