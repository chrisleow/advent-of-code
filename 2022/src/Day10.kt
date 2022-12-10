fun main() {

    data class Instruction(val type: String, val parameter: Int)
    data class State(val cycle: Int, val duringX: Int, val afterX: Int)

    fun List<String>.parse() = buildList {
        this@parse.forEach { line ->
            val parts = line.trim().split(" ")
            if (parts.isEmpty() || parts[0].isBlank()) {
                return@forEach
            }
            add(Instruction(parts[0], if (parts.size > 1) parts[1].toInt() else 0))
        }
    }

    fun State.applyInstruction(instruction: Instruction) = buildList {
        when (instruction.type) {
            "addx" -> {
                add(State(cycle = cycle + 1, duringX = afterX, afterX = afterX))
                add(State(cycle = cycle + 2, duringX = afterX, afterX = afterX + instruction.parameter))
            }
            "noop" -> {
                add(State(cycle = cycle + 1, duringX = afterX, afterX = afterX))
            }
            else -> error("bad instruction")
        }
    }

    fun List<Instruction>.getStates() = sequence {
        this@getStates.fold(State(0, 1, 1)) { state, instruction ->
            state
                .applyInstruction(instruction)
                .also { nextStates -> yieldAll(nextStates) }
                .last()
        }
    }
    fun part1(input: List<String>) = input
        .parse()
        .getStates()
        .filter { it.cycle in listOf(20, 60, 100, 140, 180, 220) }
        .sumOf { it.cycle * it.duringX }

    fun part2(input: List<String>) = buildString {
        input.parse()
            .getStates()
            .forEach { state ->
                val displayX = (state.cycle - 1) % 40
                append(if (displayX in (state.duringX - 1 .. state.duringX + 1)) "\u2588" else " ")
                if (state.cycle % 40 == 0) {
                    appendLine()
                }
            }
    }


    val testInput = readInput("Day10_test")
    check(part1(testInput) == 13140)

    val input = readInput("Day10")
    println("Part 1: ${part1(input)}")
    println("Part 2:\n${part2(input)}")
}