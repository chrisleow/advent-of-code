fun main() {

    data class Instruction(val count: Int, val source: Int, val target: Int)

    val instructionRegex = "move\\s+(\\d+)\\s+from\\s+(\\d+)\\s+to\\s+(\\d+)".toRegex()

    fun List<String>.parse(): Pair<Map<Int, String>, List<Instruction>> {
        val sections = this.split { it.isBlank() }
        return Pair(
            sections[0]
                .flatMap { line ->
                    line.mapIndexedNotNull { x, char ->
                        if (char.isLetter()) {
                            Pair(((x - 1) / 4) + 1, char)
                        } else {
                            null
                        }
                    }
                }
                .groupBy { it.first }
                .mapValues { (_, indexChars) ->
                    indexChars.joinToString("") { (_, c) -> c.toString() }
                },
            sections[1]
                .mapNotNull { line -> instructionRegex.matchEntire(line)?.groupValues }
                .map { gv -> Instruction(gv[1].toInt(), gv[2].toInt(), gv[3].toInt()) },
        )
    }

    fun Map<Int, String>.apply(instruction: Instruction, model: Int) = this
        .mapValues { (index, stack) ->
            when (index) {
                instruction.source -> {
                    stack.substring(instruction.count)
                }
                instruction.target -> {
                    val transferChars = this[instruction.source]!!.substring(0, instruction.count)
                    when (model) {
                        9000 -> transferChars.reversed() + stack
                        9001 -> transferChars + stack
                        else -> error("Unknown model number.")
                    }
                }
                else -> stack
            }
        }

    fun Map<Int, String>.getFinalString(instructions: List<Instruction>, model: Int) = instructions
        .fold(this) { stacks, instruction -> stacks.apply(instruction, model) }
        .entries
        .sortedBy { it.key }
        .joinToString("") { it.value.first().toString() }

    fun part1(input: List<String>) = input
        .parse()
        .let { (stacks, instructions) -> stacks.getFinalString(instructions, 9000) }

    fun part2(input: List<String>) = input
        .parse()
        .let { (stacks, instructions) -> stacks.getFinalString(instructions, 9001) }

    val testInput = readInput("Day05_test")
    check(part1(testInput) == "CMZ")
    check(part2(testInput) == "MCD")

    val input = readInput("Day05")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
