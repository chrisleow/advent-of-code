import java.util.*
import kotlin.math.abs

fun main() {

    data class Amphipod(
        val char: Char,
        val homeX: Int,
        val x: Int,
        val y: Int,
    )
    data class State(
        val amphipods: Set<Amphipod>,
        val burrowDepth: Int,
        val cost: Int,
        val previous: State?,
    )

    val amphipodHomeXs = mapOf('A' to 3, 'B' to 5, 'C' to 7, 'D' to 9)
    val amphipodUnitCosts = mapOf('A' to 1, 'B' to 10, 'C' to 100, 'D' to 1000)

    fun List<String>.parse() = this
        .filter { it.isNotBlank() }
        .flatMapIndexed { y, line ->
            line.mapIndexedNotNull { x, char ->
                if (char in listOf('A', 'B', 'C', 'D')) {
                    Amphipod(char, amphipodHomeXs[char]!!,  x, y)
                } else {
                    null
                }
            }
        }
        .toSet()
        .let { amphipods -> State(amphipods, amphipods.maxOf { it.y } - 1, 0, null) }

    val debugTemplateCharMap = """
        #############
        #...........#
        ###.#.#.#.###
          #.#.#.#.#
          #########
        """
        .trimIndent()
        .split("\n")
        .flatMapIndexed { y, line ->
            line.mapIndexed { x, c -> Pair(x, y) to c }.filter { (_, c) -> !c.isWhitespace() }
        }
        .toMap()

    fun State.toDebugString(): String {
        val cost = this.cost
        val pointMap = debugTemplateCharMap + this.amphipods.map { a -> Pair(a.x, a.y) to a.char }
        val maxX = pointMap.keys.maxOf { it.first }
        val maxY = pointMap.keys.maxOf { it.second }
        return buildString {
            (0..maxY).forEach { y ->
                appendLine(
                    (0 .. maxX).joinToString("") { x -> (pointMap[Pair(x, y)] ?: ' ').toString() }
                )
            }
            appendLine("Cost: $cost")
        }
    }

    fun State.move(amphipod: Amphipod, target: Pair<Int, Int>): State = this.copy(
        amphipods = this.amphipods
            .map { a -> if (a == amphipod) a.copy(x = target.first, y = target.second) else a }
            .toSet(),
        cost = this.cost + (amphipodUnitCosts[amphipod.char]!! *
                (abs(amphipod.x - target.first) + abs(amphipod.y - target.second))),
        previous = this,
    )

    fun State.getMoves(): List<State> {
        val state = this
        val amphipodColumns = state.amphipods.groupBy { it.x }

        val burrowTargetYs = listOf(3 to 'A', 5 to 'B', 7 to 'C', 9 to 'D')
            .filter { (x, c) -> amphipodColumns[x]?.all { a -> a.char == c } ?: true }
            .map { (x, _) -> x to ((amphipodColumns[x]?.minOf { it.y } ?: (this.burrowDepth + 2)) - 1) }
            .filter { (_, y) -> y > 1 }
            .toMap()

        val burrowSourceAmphipods = listOf(3, 5, 7, 9)
            .mapNotNull { x -> amphipodColumns[x]?.minBy { it.y } }
            .associateBy { a -> a.x }

        data class FoldState(
            val homeAmphipod: Amphipod?,
            val homeFreeIndexes: List<Int>,
            val nextStates: List<State>,
        )

        val leftScanNextStates = (1 .. 9)
            .fold(FoldState(null, emptyList(), emptyList())) { foldState, x ->
                if (x == 3 || x == 5 || x == 7 || x == 9) {
                    foldState.copy(
                        nextStates = foldState.nextStates + sequence {

                            // move (left) home amphipod into burrow, if this makes sense
                            if (foldState.homeAmphipod != null) {
                                if (foldState.homeAmphipod.homeX == x) {
                                    val targetY = burrowTargetYs[x]
                                    if (targetY != null) {
                                        yield(state.move(foldState.homeAmphipod, Pair(x, targetY)))
                                    }
                                }
                            }

                            // move amphipod out of burrow, going left
                            burrowSourceAmphipods[x]?.let { burrowAmphipod ->
                                foldState.homeFreeIndexes.forEach { homeX ->
                                    yield(state.move(burrowAmphipod, Pair(homeX, 1)))
                                }
                            }
                        }
                    )
                } else {
                    when (val newHomeAmphipod = amphipodColumns[x]?.single()) {
                        null -> foldState.copy(
                            homeFreeIndexes = foldState.homeFreeIndexes + x,
                        )
                        else -> foldState.copy(
                            homeAmphipod = newHomeAmphipod,
                            homeFreeIndexes = emptyList(),
                        )
                    }
                }
            }
            .nextStates

        val rightScanNextStates = (11 downTo 3)
            .fold(FoldState(null, emptyList(), emptyList())) { foldState, x ->
                if (x == 3 || x == 5 || x == 7 || x == 9) {
                    foldState.copy(
                        nextStates = foldState.nextStates + sequence {

                            // move (right) home amphipod into burrow, if this makes sense
                            if (foldState.homeAmphipod != null) {
                                if (foldState.homeAmphipod.homeX == x) {
                                    val targetY = burrowTargetYs[x]
                                    if (targetY != null) {
                                        yield(state.move(foldState.homeAmphipod, Pair(x, targetY)))
                                    }
                                }
                            }

                            // move amphipod out of burrow, going left
                            burrowSourceAmphipods[x]?.let { burrowAmphipod ->
                                foldState.homeFreeIndexes.forEach { homeX ->
                                    yield(state.move(burrowAmphipod, Pair(homeX, 1)))
                                }
                            }
                        }
                    )
                } else {
                    when (val newHomeAmphipod = amphipodColumns[x]?.single()) {
                        null -> foldState.copy(
                            homeFreeIndexes = foldState.homeFreeIndexes + x,
                        )
                        else -> foldState.copy(
                            homeAmphipod = newHomeAmphipod,
                            homeFreeIndexes = emptyList(),
                        )
                    }
                }
            }
            .nextStates

        // put everything
        return leftScanNextStates + rightScanNextStates
    }

    fun State.getSolvedCost(printTrail: Boolean): Int {
        val seenStateSignatures = mutableSetOf<Set<Amphipod>>()
        val queue = PriorityQueue<State>(compareBy { it.cost })
        queue.add(this)

        while (queue.isNotEmpty()) {
            val state = queue.remove()
            if (state.amphipods.all { it.x == it.homeX }) {
                if (printTrail) {
                    val backwardsStateTrail = sequence {
                        var current: State? = state
                        while (current != null) {
                            yield(current)
                            current = current.previous
                        }
                    }
                    backwardsStateTrail
                        .toList()
                        .reversed()
                        .forEach { println(it.toDebugString()) }
                }
                return state.cost
            }

            // shortcut if we've been here before
            if (state.amphipods in seenStateSignatures) {
                continue
            } else {
                seenStateSignatures.add(state.amphipods)
            }

            // keep searching
            state.getMoves().forEach { queue.add(it) }
        }

        error("Completely run out of moves.")
    }

    fun part1(input: List<String>, printTrail: Boolean) = input
        .parse()
        .getSolvedCost(printTrail)

    val part2ExtraLines = listOf(
        "  #D#C#B#A#",
        "  #D#B#A#C#",
    )

    fun part2(input: List<String>, printTrail: Boolean) = input
        .let { ls -> ls.slice(0 .. 2) + part2ExtraLines + ls.slice(3 .. 4) }
        .parse()
        .getSolvedCost(printTrail)

    // test
    val testInput = readInput("Day23_test")
    check(part1(testInput, false) == 12521)
    println("Checked Part 1 ... ok.")
    check(part2(testInput, false) == 44169)
    println("Checked Part 2 ... ok.")

    val input = readInput("Day23")
    println("Part 1: ${part1(input, true)}")
    println()
    println("Part 2: ${part2(input, true)}")
}
