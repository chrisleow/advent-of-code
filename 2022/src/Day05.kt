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

    fun Map<Int, String>.apply(instruction: Instruction, model: Int) = this + listOf(
        instruction.source to run {
            (this[instruction.source] ?: error("Invalid index ${instruction.source}."))
                .substring(instruction.count)
        },
        instruction.target to run {
            val targetStack = (this[instruction.target] ?: error("Invalid index ${instruction.source}."))
            val transferChars = (this[instruction.source] ?: error("Invalid index ${instruction.source}."))
                .substring(0, instruction.count)
            when (model) {
                9000 -> transferChars.reversed() + targetStack
                9001 -> transferChars + targetStack
                else -> error("Unknown model number.")
            }
        }
    )

    fun part1(input: List<String>): String {
        val (initialStacks, instructions) = input.parse()
        return instructions
            .fold(initialStacks) { stacks, instruction -> stacks.apply(instruction, 9000) }
            .entries
            .sortedBy { it.key }
            .joinToString("") { it.value.first().toString() }
    }

    fun part2(input: List<String>): String {
        val (initialStacks, instructions) = input.parse()
        return instructions
            .fold(initialStacks) { stacks, instruction -> stacks.apply(instruction, 9001) }
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
