fun main() {

    data class Instruction(val count: Int, val source: Int, val target: Int)

    val instructionRegex = "move\\s+(\\d+)\\s+from\\s+(\\d+)\\s+to\\s+(\\d+)".toRegex()

    fun List<String>.parse(): Pair<Map<Int, String>, List<Instruction>> {
        val sections = this.split { it.isBlank() }

        val initialStacks = sections[0]
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
            }

        val instructions = sections[1]
            .mapNotNull { line -> instructionRegex.matchEntire(line)?.groupValues }
            .map { gv -> Instruction(gv[1].toInt(), gv[2].toInt(), gv[3].toInt()) }

        return Pair(initialStacks, instructions)
    }

    fun Map<Int, String>.apply9000(instruction: Instruction) = this.mapValues { (index, stack) ->
        when (index) {
            instruction.source -> {
                stack.substring(instruction.count)
            }
            instruction.target -> {
                val transferChars = (this[instruction.source] ?: error("shouldn't get here."))
                    .substring(0 until instruction.count)
                    .reversed()
                transferChars + stack
            }
            else -> stack
        }
    }

    fun Map<Int, String>.apply9001(instruction: Instruction) = this.mapValues { (index, stack) ->
        when (index) {
            instruction.source -> {
                stack.substring(instruction.count)
            }
            instruction.target -> {
                val transferChars = (this[instruction.source] ?: error("shouldn't get here."))
                    .substring(0 until instruction.count)
                transferChars + stack
            }
            else -> stack
        }
    }

    fun part1(input: List<String>): String {
        val (initialStacks, instructions) = input.parse()
        return instructions
            .fold(initialStacks) { stacks, instruction -> stacks.apply9000(instruction) }
            .entries
            .sortedBy { it.key }
            .joinToString("") { it.value.first().toString() }
    }

    fun part2(input: List<String>): String {
        val (initialStacks, instructions) = input.parse()
        return instructions
            .fold(initialStacks) { stacks, instruction -> stacks.apply9001(instruction) }
            .entries
            .sortedBy { it.key }
            .joinToString("") { it.value.first().toString() }
    }

    val testInput = readInput("Day05_test")
    check(part1(testInput) == "CMZ")
    check(part2(testInput) == "MCD")

    val input = readInput("Day05")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
