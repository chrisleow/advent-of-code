fun main() {

    data class Pawn(val id: Int, val position: Int, val score: Int)
    data class State(val pawns: List<Pawn>, val rolls: Int)

    fun parseInput(input: List<String>): State {
        val regex = "Player (\\d+) starting position: (\\d+)".toRegex()
        return State(
            pawns = input
                .mapNotNull { regex.matchEntire(it.trim())?.groupValues }
                .map { gv -> Pawn(id = gv[1].toInt(), position = gv[2].toInt(), score = 0) },
            rolls = 0,
        )
    }

    tailrec fun State.play(): State = when {
        pawns.any { it.score >= 1000 } -> this
        else -> {
            val pawn = pawns.first()
            val rollTotal = (rolls until rolls + 3).sumOf { (it % 100) + 1 }
            val newPosition = ((pawn.position + rollTotal - 1) % 10) + 1
            val newPawn = pawn.copy(position = newPosition, score = pawn.score + newPosition)
            State(pawns.drop(1) + newPawn, rolls + 3).play()
        }
    }

    // permutations of dice required to get the roll number
    val diracRollTotals = listOf(3 to 1, 4 to 3, 5 to 6, 6 to 7, 7 to 6, 8 to 3, 9 to 1)

    fun State.isFinished() = this.pawns.any { it.score >= 21 }

    // count "universes", note that we ignore rolls (set at -1) as this information isn't needed in the result
    tailrec fun Map<State, Long>.play(): Map<State, Long> {
        if (keys.all { state -> state.isFinished() }) {
            return this
        }

        return entries
            .flatMap { (state, count) ->
                if (state.isFinished()) {
                    listOf(state to count)
                } else {
                    val pawn = state.pawns.first()
                    diracRollTotals.map { (rollTotal, rollCount) ->
                        val newPosition = ((pawn.position + rollTotal - 1) % 10) + 1
                        val newPawn = pawn.copy(position = newPosition, score = pawn.score + newPosition)
                        val newRolls = state.rolls + 3
                        val newState = State(state.pawns.drop(1) + newPawn, newRolls)
                        Pair(newState, count * rollCount)
                    }
                }
            }
            .groupBy({ (state, _) -> state }) { (_, count) -> count }
            .mapValues { (_, counts) -> counts.sum() }
            .play()
    }

    fun part1(input: List<String>): Int {
        val finalState = parseInput(input).play()
        return finalState.pawns.minOf { it.score } * finalState.rolls
    }

    fun part2(input: List<String>): Long {
        val finalMap = mapOf(parseInput(input) to 1L).play()
        val winnerIdCounts = finalMap.entries
            .groupBy { (state, _) -> state.pawns.maxByOrNull { it.score }?.id ?: error("need a winner!") }
            .mapValues { (_, pairs) -> pairs.sumOf { (_, count) -> count } }
        return winnerIdCounts.maxOf { (_, count) -> count }
    }

    // test
    val testInput = readInput("Day21_test")
    check(part1(testInput) == 739785)
    check(part2(testInput) == 444356092776315L)

    val input = readInput("Day21")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
