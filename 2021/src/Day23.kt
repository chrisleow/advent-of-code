import java.util.*
import kotlin.math.abs

fun main() {

    // note that empty positions are marked as empty chars, and an amphipod is marked in capitals
    // to indicate it's moved.
    data class State(val rooms: Map<Int, String>, val hallway: String)
    data class Solution(val path: List<State>, val cost: Int)

    fun State.toDebugString() = buildString {
        val state = this@toDebugString
        val length = state.hallway.length + 2

        // room positions
        val roomDisplayPositions = state.rooms.keys.flatMap { (it - 1 .. it + 1) }.toSet()

        // print out
        (0 until length).forEach { append("#") }; appendLine()
        append("#"); append(state.hallway); appendLine("#")
        (0 until rooms.values.maxOf { it.length }).forEach { depth ->
            (0 until length).forEach { pos ->
                append(
                    rooms[pos - 1]
                        ?.get(depth)
                        ?.uppercaseChar()
                        ?: (if (depth == 0 || pos - 1 in roomDisplayPositions) '#' else ' ')
                )
            }
            appendLine()
        }
        (0 until length).forEach { if (it - 1 in roomDisplayPositions) append("#") else append(" ") }
        appendLine()
    }

    fun Solution.toDebugString() = buildString {
        path.forEach { appendLine(it.toDebugString()) }
        appendLine(">>> Cost: $cost")
    }

    fun Char.getCostMultiplier(): Int = when (this.uppercaseChar()) {
        'A' -> 1
        'B' -> 10
        'C' -> 100
        'D' -> 1000
        else -> error("not a valid amphipod")
    }

    fun Char.getHomePosition(): Int = when (this.uppercaseChar()) {
        'A' -> 2
        'B' -> 4
        'C' -> 6
        'D' -> 8
        else -> error("not a valid amphipod")
    }

    fun parseInput(input: List<String>): State {
        val charLines = input
            .map { line -> line.filter { it in "ABCD" } }
            .filter { it.isNotEmpty() }
        return State(
            hallway = (0 .. 10).joinToString("") { "." },
            rooms = "ABCD"
                .mapIndexed { index, char -> char to charLines.joinToString("") { it[index].toString() } }
                .associate { (char, room) -> char.getHomePosition() to room.lowercase() }
        )
    }

    fun <T> List<T>.replaceAt(replacement: T, index: Int) = this
        .mapIndexed { pos, item -> if (pos == index) replacement else item }

    fun String.replaceAt(replacement: Char, index: Int) = this
        .toList()
        .replaceAt(replacement, index)
        .joinToString("") { it.toString() }

    // make a move (assuming move is legitimate to make)
    fun State.moveToHallway(roomPosition: Int, hallwayPosition: Int): Pair<State, Int>? {
        val room = rooms[roomPosition]
            ?: return null
        val (depth, typeChar) = room.withIndex().firstOrNull { (_, char) -> char != '.' }
            ?: return null

        // can't move an already moved amphipod
        if (typeChar.isUpperCase()) {
            return null
        }

        // check for a clear path
        val minPosition = minOf(hallwayPosition, roomPosition)
        val maxPosition = maxOf(hallwayPosition, roomPosition)
        if ((minPosition .. maxPosition).any { hallway[it] != '.' }) {
            return null
        }

        // attempt to make a move
        val distance = abs(roomPosition - hallwayPosition) + depth + 1
        val state = State(
            rooms = rooms + (roomPosition to room.replaceAt('.', depth)),
            hallway = hallway.replaceAt(typeChar.uppercaseChar(), hallwayPosition),
        )
        return Pair(state, distance * typeChar.getCostMultiplier())
    }

    fun State.moveToRoom(hallwayPosition: Int): Pair<State, Int>? {
        val typeChar = when (val char = hallway[hallwayPosition]) {
            '.' -> return null
            else -> char
        }
        val roomPosition = typeChar.getHomePosition()
        val room = rooms[roomPosition] ?: return null
        val depth = room.indices.lastOrNull { room[it] == '.' } ?: return null

        // check for a clear path
        val minPosition = minOf(hallwayPosition, roomPosition)
        val maxPosition = maxOf(hallwayPosition, roomPosition)
        if ((minPosition .. maxPosition).any { it != hallwayPosition && hallway[it] != '.' }) {
            return null
        }

        // attempt to make a move
        val distance = abs(roomPosition - hallwayPosition) + depth + 1
        val state = State(
            rooms = rooms + (roomPosition to room.replaceAt(typeChar.uppercaseChar(), depth)),
            hallway = hallway.replaceAt('.', hallwayPosition),
        )
        return Pair(state, distance * typeChar.getCostMultiplier())
    }

    fun State.nextMoves(): Sequence<Pair<State, Int>> = sequence {

        // examine room -> hallway for existing rooms
        rooms.keys.forEach { roomPosition ->

            // go left (only if there's nothing in the way)
            (roomPosition downTo 0)
                .filter { pos -> pos !in rooms.keys }
                .map { pos -> moveToHallway(roomPosition, pos) }
                .takeWhile { it != null }
                .forEach { yield(it!!) }

            // go right (only if there's nothing in the way)
            (roomPosition until hallway.length)
                .filter { pos -> pos !in rooms.keys }
                .map { pos -> moveToHallway(roomPosition, pos) }
                .takeWhile { it != null }
                .forEach { yield(it!!) }
        }

        // examine hallway -> room for moves
        hallway.indices
            .mapNotNull { pos -> moveToRoom(pos) }
            .forEach { yield(it) }
    }

    fun State.isSolution(): Boolean {
        val conditions = listOf(
            hallway.all { it == '.' },
            rooms.all { (pos, room) -> room.all { it != '.' && it.getHomePosition() == pos } },
        )
        return conditions.all { it }
    }

    fun State.solve(): Solution {
        val backPointers = mutableMapOf<State, State>()
        val costs = mutableMapOf(this to 0)
        val queue = PriorityQueue<Pair<Int, State>>(compareBy { it.first }).also { it.add(0 to this) }
        while (true) {
            val (costSoFar, state) = queue.remove() ?: error("no more states to search")
            if (state.isSolution()) {
                return Solution(
                    cost = costSoFar,
                    path = generateSequence(state) { backPointers[it] }.asIterable().reversed(),
                )
            }

            state.nextMoves().forEach { (nextState, cost) ->
                if (costSoFar + cost < (costs[nextState] ?: Int.MAX_VALUE)) {
                    costs[nextState] = costSoFar + cost
                    backPointers[nextState] = state
                    queue.add(costSoFar + cost to nextState)
                }
            }
        }
    }

    fun part1(input: List<String>): Int {
        return parseInput(input).solve().cost
    }

    fun part2(input: List<String>): Int {
        val interpolated = mapOf(2 to "DD", 4 to "CB", 6 to "BA", 8 to "AC")
        val inputState = parseInput(input)
        val state = inputState.copy(
            rooms = inputState.rooms.mapValues { (pos, room) ->
                "${room[0]}${interpolated[pos]?.lowercase()}${room[1]}"
            }
        )
        return state.solve().cost
    }

    // test
    val testInput = readInput("Day23_test")
    check(part1(testInput) == 12521)

    val input = readInput("Day23")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
