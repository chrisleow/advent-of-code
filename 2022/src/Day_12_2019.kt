fun main() {

    fun List<String>.parse(): Pair<Set<Int>, Set<Set<Int>>> {
        val sections = this.dropWhile { it.isBlank() }.split { it.isBlank() }
        val ruleRegex = "([#.]*)\\s+=>\\s+#".toRegex()
        return Pair(
            sections[0]
                .joinToString("") { line -> line.filter { it in "#." } }
                .mapIndexedNotNull { index, c -> if (c == '#') index else null }
                .toSet(),
            sections[1]
                .mapNotNull { ruleRegex.matchEntire(it.trim())?.groupValues?.get(1) }
                .map { p -> p.mapIndexedNotNull { idx, c -> if (c == '#') idx - 2 else null }.toSet() }
                .toSet(),
        )
    }

    fun Set<Int>.next(survivors: Set<Set<Int>>) =  (this.min() - 2 .. this.max() + 2)
        .filter { index ->
            val neighboursAndSelf = (index - 2 .. index + 2)
                .filter { it in this }
                .map { it - index }
                .toSet()
            (neighboursAndSelf in survivors)
        }
        .toSet()

    fun part1(input: List<String>): Int {
        val (initialState, survivors) = input.parse()
        return generateSequence(initialState) { state -> state.next(survivors) }
            .drop(20)
            .first()
            .sumOf { it }
    }

    fun Set<Int>.getDistances() = this
        .sorted()
        .windowed(2)
        .map { values -> values[1] - values[0] }

    fun part2(input: List<String>): Long {
        val (initialState, survivors) = input.parse()
        val sameIndexedPatternSets = generateSequence(initialState) { state -> state.next(survivors) }
            .withIndex()
            .windowed(2)
            .first { indexedSets -> indexedSets[0].value.getDistances() == indexedSets[1].value.getDistances() }

        val (index0, set0) = sameIndexedPatternSets[0]
        val (_, set1) = sameIndexedPatternSets[1]
        val indexDelta = set1.min() - set0.min()
        return set0.sumOf { it + ((50_000_000_000L - index0) * indexDelta) }
    }

    val testInput = readInput("Day_12_2019_test")
    check(part1(testInput) == 325)
    check(part2(testInput) == 999999999374L)

    val input = readInput("Day_12_2019")
    println("Part 1 ${part1(input)}")
    println("Part 2 ${part2(input)}")
}