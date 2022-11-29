// brute-force solution takes about 30 mins to run, as I'm too lazy to disassemble the code
//
fun main() {

    data class Instruction(val command: String, val first: String, val second: String)
    data class InstructionBlock(val inputRegister: Char, val instructions: List<Instruction>)
    data class LowestHighest(val lowest: String, val highest: String)

    fun parseInput(input: List<String>): List<InstructionBlock> {
        val regex = "(\\w+)\\s+(\\w+)\\s*([0-9wxyz-]+)?".toRegex()
        return input
            .mapNotNull { regex.matchEntire(it.trim())?.groupValues }
            .map { gv -> Instruction(gv[1], gv[2], gv[3]) }
            .fold(emptyList()) { blocks, instruction ->
                if (instruction.command == "inp") {
                    blocks + listOf(InstructionBlock(instruction.first.first(), emptyList()))
                } else {
                    blocks.dropLast(1) + blocks.last().copy(instructions = blocks.last().instructions + instruction)
                }
            }
    }

    fun LowestHighest?.update(digits: String): LowestHighest {
        return when(this) {
            null -> LowestHighest(digits, digits)
            else -> LowestHighest(minOf(lowest, digits), maxOf(highest, digits))
        }
    }

    // simulate for z
    fun InstructionBlock.simulate(w: Int, z: Int): Int? {
        val finalMem = instructions.fold(mapOf('w' to w, 'z' to z, 'x' to 0, 'y' to 0)) { mem, instruction ->
            val existing = mem[instruction.first.first()] ?: error("should have a memory entry")
            val operand = mem[instruction.second.first()] ?: instruction.second.toInt()
            val replacement = when (instruction.command) {
                "add" -> existing + operand
                "mul" -> existing * operand
                "div" -> if (operand != 0) existing / operand else return null
                "mod" -> if (existing >= 0 && operand > 0) existing % operand else return null
                "eql" -> if (existing == operand) 1 else 0
                else -> error("unrecognised command")
            }
            mem + (instruction.first.first() to replacement)
        }
        return finalMem['z']
    }

    fun simulateAll(input: List<String>): LowestHighest {
        val instructionBlocks = parseInput(input)
        val lowestHighestMaps = instructionBlocks
            .reversed()
            .foldIndexed(mapOf(0 to LowestHighest("", ""))) { index, zDigits, block ->
                val newZDigits = (1 .. 9).asSequence().flatMap { w ->
                    (0 .. 1_000_000).asSequence().flatMap { z ->
                        zDigits[block.simulate(w, z) ?: -1]
                            ?.let { listOf(z to "${w}${it.lowest}", z to "${w}${it.highest}") }
                            ?: emptyList()
                    }
                }
                newZDigits
                    .groupingBy { (z, _) -> z }
                    .aggregate { _, lh: LowestHighest?, (_, digits), _ -> lh.update(digits) }
                    .also { println("Instruction Group ${index + 1} / ${instructionBlocks.size}, Size: ${it.size}") }
            }

        // go for lowest / highest
        return lowestHighestMaps[0] ?: error("no sane position to start with z == 0")
    }

    val input = readInput("Day24")
    println("Lowest / Highest: ${simulateAll(input)}")
}
