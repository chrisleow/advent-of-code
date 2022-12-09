import java.util.PriorityQueue

fun main() {

    data class ItemFloors(
        val generator: Int,
        val microchip: Int,
    )

    data class State(
        val elevator: Int,
        val itemFloors: Map<String, ItemFloors>,
        val steps: Int,
        val previous: State?,
    )

    fun List<String>.parse(): State {
        val lines = this.filter { it.isNotBlank() }
        val generatorRegex = "(\\w+) generator".toRegex()
        val microchipRegex = "(\\w+)-compatible".toRegex()
        return State(
            elevator = 0,
            itemFloors = lines
                .flatMapIndexed { index, line ->
                    sequence {
                        yieldAll(
                            generatorRegex
                                .findAll(line)
                                .map { it.groupValues[1] to ItemFloors(index, -1) }
                        )
                        yieldAll(
                            microchipRegex
                                .findAll(line)
                                .map { it.groupValues[1] to ItemFloors(-1, index) }
                        )
                    }
                }
                .groupBy({ (item, _) -> item }) { (_, itemFloors) -> itemFloors }
                .mapValues { (_, floors) ->
                    floors.fold(ItemFloors(-1, -1)) { (a0, a1), (p0, p1) ->
                        ItemFloors(maxOf(a0, p0), maxOf(a1, p1))
                    }
                }
                .toMap(),
            steps = 0,
            previous = null,
        )
    }

    fun State.addItems(items: List<Pair<String, ItemFloors>>) =
        this.copy(itemFloors = this.itemFloors + items)

    fun State.getSignature() = buildString {
        val state = this@getSignature
        append("[${state.elevator}]")
        state.itemFloors.values
            .map { "${it.generator}/${it.microchip}" }
            .sorted()
            .forEach { append(":$it") }
    }

    fun State.move(generators: List<String>, microchips: List<String>, destination: Int) = this.copy(
        elevator = destination,
        itemFloors = this.itemFloors.mapValues { (item, floors) ->
            ItemFloors(
                generator = if (item in generators) destination else floors.generator,
                microchip = if (item in microchips) destination else floors.microchip,
            )
        },
        steps = this.steps + 1,
        previous = this,
    )

    fun State.isValid(): Boolean {
        val unsafeFloors = this.itemFloors.values.map { it.generator }.distinct()
        return this.itemFloors.all { (_, floors) ->
            (floors.microchip !in unsafeFloors || floors.generator == floors.microchip)
        }
    }

    fun State.generateMoves(): List<State> {
        val state = this@generateMoves
        val floorItemTypePairs = state.itemFloors.entries
            .flatMap { (item, floors) ->
                listOfNotNull(
                    if (state.elevator == floors.generator) Pair(item, 'G') else null,
                    if (state.elevator == floors.microchip) Pair(item, 'M') else null,
                )
            }

        return floorItemTypePairs
            .flatMap { ft1 -> floorItemTypePairs.map { ft2 -> setOf(ft1, ft2) } }
            .distinct()
            .map { floorItemTypes ->
                Pair(
                    floorItemTypes.filter { (_, c) -> c == 'G' }.map { (item, _) -> item },
                    floorItemTypes.filter { (_, c) -> c == 'M' }.map { (item, _) -> item },
                )
            }
            .flatMap { (generators, microchips) ->
                buildList {
                    if (state.elevator < 3) {
                        add(state.move(generators, microchips, state.elevator + 1))
                    }
                    if (state.elevator > 0) {
                        add(state.move(generators, microchips, state.elevator - 1))
                    }
                }
            }
            .filter { it.isValid() }
    }

    fun State.solve(printSolution: Boolean): Int {
        val seenSignatures = mutableSetOf<String>()
        val queue = PriorityQueue<State>(compareBy { it.steps })
        queue.add(this)

        while (queue.isNotEmpty()) {
            val state = queue.remove() ?: continue
            if (state.itemFloors.values.all { (gf, mf) -> gf == 3 && mf == 3 }) {
                if (printSolution) {
                    generateSequence(state) { it.previous }
                        .toList()
                        .reversed()
                        .forEach { previousState ->
                            val stepsString = previousState.steps.toString().padStart(3, ' ')
                            println(" ${stepsString}: ${previousState.getSignature()}")
                        }
                }
                return state.steps
            }

            val signature = state.getSignature()
            if (signature in seenSignatures) {
                continue
            } else {
                seenSignatures.add(signature)
            }

            state
                .generateMoves()
                .forEach { nextState -> queue.add(nextState) }
        }

        error("Shouldn't get here.")
    }

    fun part1(input: List<String>, printDebug: Boolean) = input
        .parse()
        .solve(printDebug)

    fun part2(input: List<String>, printDebug: Boolean) = input
        .parse()
        .addItems(
            listOf(
                "elerium" to ItemFloors(0, 0),
                "dilithium" to ItemFloors(0, 0),
            )
        )
        .solve(printDebug)

    val testInput = readInput("Day_11_2016_test")
    check(part1(testInput, false) == 11)

    val input = readInput("Day_11_2016")
    println("Part 1: ${part1(input, true)}")
    println("Part 2: ${part2(input, true)}")
}