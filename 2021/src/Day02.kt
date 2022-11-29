sealed class Direction {
    object Forward : Direction()
    object Down : Direction()
    object Up : Direction()
}

fun main() {

    fun readInstructions(input: List<String>): List<Pair<Direction, Int>> {
        val regex = "(\\w+)\\s+(\\d+)".toRegex()
        return input.mapNotNull {
            regex.matchEntire(it)?.let { match ->
                Pair(
                    when (val direction = match.groupValues[1]) {
                        "forward" -> Direction.Forward
                        "up" -> Direction.Up
                        "down" -> Direction.Down
                        else -> error("Invalid Direction '$direction'.")
                    },
                    match.groupValues[2].toInt(),
                )
            }
        }
    }

    fun part1(input: List<String>): Int {
        val instructions = readInstructions(input)

        data class State(val pos: Int, val depth: Int)

        val initialState = State(0, 0)
        val finalState = instructions.fold(initialState) { state, instruction ->
            val (direction, units) = instruction
            when (direction) {
                Direction.Forward -> state.copy(pos = state.pos + units)
                Direction.Up -> state.copy(depth = state.depth - units)
                Direction.Down -> state.copy(depth = state.depth + units)
            }
        }

        return finalState.pos * finalState.depth
    }

    fun part2(input: List<String>): Int {
        val instructions = readInstructions(input)

        data class State(val pos: Int, val depth: Int, val aim: Int)

        val initialState = State(0, 0, 0)
        val finalState = instructions.fold(initialState) { state, instruction ->
            val (direction, units) = instruction
            when (direction) {
                Direction.Forward -> state.copy(
                    pos = state.pos + units,
                    depth = state.depth + (state.aim * units),
                )
                Direction.Up -> state.copy(aim = state.aim - units)
                Direction.Down -> state.copy(aim = state.aim + units)
            }
        }

        return finalState.pos * finalState.depth
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part1(testInput) == 150)
    check(part2(testInput) == 900)

    val input = readInput("Day02")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
