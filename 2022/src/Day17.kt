import java.util.PriorityQueue

fun main() {

    data class Valve(
        val room: String,
        val flowRate: Int,
        val linkedRooms: List<String>,
    )
    data class State(
        val remaining: Int,
        val rooms: List<String>,
        val openedValves: Set<String>,
        val flowRate: Int,
        val pressureReleased: Int,
        val previous: State?,
    )

    fun List<String>.parse(): List<Valve> {
        val regex = ".*([A-Z]{2}).*=(\\d+).*valves?\\s+([A-Z, ]+)".toRegex()
        return this
            .mapNotNull { line -> regex.matchEntire(line)?.groupValues }
            .map { gv -> Valve(gv[1], gv[2].toInt(), gv[3].split(",").map { it.trim() }) }
    }

    fun <T, R> List<T>.crossFlatMap(block: (T) -> Iterable<R>): Sequence<List<R>> = sequence {
        val list = this@crossFlatMap
        if (list.isEmpty()) {
            yield(emptyList())
        } else {
            block(list.first()).forEach { mappedValue ->
                val mappedValueList = listOf(mappedValue)
                list.drop(1).crossFlatMap(block).forEach { yield(mappedValueList + it) }
            }
        }
    }

    fun State.generateMoves(valveByRoom: Map<String, Valve>) = sequence {
        val state = this@generateMoves
        if (state.remaining <= 0) {
            return@sequence
        }

        fun getMoves(room: String) = sequence {
            val valve = valveByRoom[room] ?: error("shouldn't get here.")

            // open a valve
            if (valve.room !in openedValves && valve.flowRate > 0) {
                yield(room to valve.flowRate)
            }

            // traverse
            valve.linkedRooms.forEach { linkedRoom ->
                yield(linkedRoom to 0)
            }

            // do nothing
            yield(room to 0)
        }

        state.rooms
            .crossFlatMap { getMoves(it).toList() }
            .forEach { moves ->
                yield(
                    state.copy(
                        remaining = remaining - 1,
                        rooms = moves.map { (r, _) -> r },
                        openedValves = openedValves + moves.filter { (_, f) -> f > 0 }.map { (r, _) -> r },
                        flowRate = flowRate + moves.distinct().sumOf { (_, f) -> f },
                        pressureReleased = pressureReleased + flowRate,
                        previous = state,
                    )
                )
            }
    }

    fun generateStates(valves: List<Valve>, startRooms: List<String>, minutes: Int): Sequence<State> {
        val valveByRoom = valves.associateBy { it.room }

        // generational promotion
        val initialStates = sequenceOf(
            State(minutes, startRooms, emptySet(), 0, 0, null)
        )
        val statesGenerations = generateSequence(initialStates) { states ->
            print(".")
            states
                .flatMap { state -> state.generateMoves(valveByRoom) }
                .distinct()
                .sortedByDescending { state -> state.pressureReleased }
                .take(10_000)
        }
        println()
        return statesGenerations.flatten()


//        val queue = PriorityQueue<State>(compareByDescending { it.remaining })
//        queue.add(State(minutes, startRooms, emptySet(), 0, 0, null))
//
//        val maxPressureReleasedByRooms = mutableMapOf<Set<String>, Int>()
//
//        while (queue.isNotEmpty()) {
//            val currentState = queue.remove() ?: return@sequence
//            yield(currentState)
//
//            // optimisation to prune the tree, don't navigate to known states apart from to "do nothing"
//            // val signature = (currentState.rooms.toSet() to currentState.openedValves)
//            val rooms = currentState.rooms.toSet()
//            val maxPressureReleasedSoFar = maxPressureReleasedByRooms[rooms] ?: -1
//            if (currentState.pressureReleased <= maxPressureReleasedSoFar) {
//                continue
//            } else {
//                maxPressureReleasedByRooms[rooms] = currentState.pressureReleased
//            }
//
//            currentState
//                .generateMoves(valveByRoom)
//                .forEach { queue.add(it) }
//        }
    }

    fun part1(input: List<String>): Int {
        val finalState = generateStates(input.parse(), listOf("AA"), 30)
            .maxBy { it.pressureReleased }

        generateSequence(finalState) { it.previous }
            .toList()
            .reversed()
            .forEach {
                println("Remaining ${it.remaining}: Rooms ${it.rooms.joinToString(", ")}, Pressure: ${it.pressureReleased}.")
            }

        return finalState.pressureReleased
    }

    fun part2(input: List<String>): Int {
        val finalState = generateStates(input.parse(), listOf("AA", "AA"), 26)
            .maxBy { it.pressureReleased }

        generateSequence(finalState) { it.previous }
            .toList()
            .reversed()
            .forEach {
                println("Remaining ${it.remaining}: Rooms ${it.rooms.joinToString(", ")}, Pressure: ${it.pressureReleased}.")
            }

        return finalState.pressureReleased
    }

    val testInput = readInput("Day16_test")
    check(part1(testInput) == 1651)
    // check(part2(testInput) == 1707)

//    val input = readInput("Day16")
//    println("Part 1: ${part1(input)}")
//    println("Part 2: ${part2(input)}")
}
