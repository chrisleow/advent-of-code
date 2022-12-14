fun main() {

    data class Instruction(val type: String, val deltaX: Int)
    data class State(val cycle: Int, val x: Int)

    fun List<String>.parse() = buildList {
        this@parse.forEach { line ->
            val parts = line.trim().split(" ")
            if (parts.isEmpty() || parts[0].isBlank()) {
                return@forEach
            }
            add(Instruction(parts[0], if (parts.size > 1) parts[1].toInt() else 0))
        }
    }

    fun List<Instruction>.getStates() = sequence {
        this@getStates.fold(Pair(0, 1)) { (cycle, x), instruction ->
            when (instruction.type) {
                "addx" -> {
                    yield(State(cycle + 1, x))
                    yield(State(cycle + 2, x))
                    Pair(cycle + 2, x + instruction.deltaX)
                }
                "noop" -> {
                    yield(State(cycle + 1, x))
                    Pair(cycle + 1, x)
                }
                else -> error("bad instruction")
            }
        }
    }

    fun part1(input: List<String>) = input
        .parse()
        .getStates()
        .filter { it.cycle in listOf(20, 60, 100, 140, 180, 220) }
        .sumOf { it.cycle * it.x }

    fun part2(input: List<String>) = buildString {
        input.parse()
            .getStates()
            .forEach { (cycle, x) ->
                if ((cycle - 1) % 40 in (x - 1 .. x + 1)) {
                    append("\u2588")
                } else {
                    append("\u00b7")
                }
                if (cycle % 40 == 0) {
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