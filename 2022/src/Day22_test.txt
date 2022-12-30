fun main() {

    abstract class Instruction
    data class Literal(val value: Long) : Instruction()
    data class Operation(val left: String, val right: String, val operation: Char) : Instruction()

    fun List<String>.parse(): Map<String, Instruction> {
        val instructionRegex = "(\\w+):\\s+(.*)".toRegex()
        val operationRegex = "(\\w+)\\s+(.)\\s+(\\w+)".toRegex()
        return this
            .mapNotNull { instructionRegex.matchEntire(it)?.groupValues }
            .associate { gv ->
                gv[1] to run {
                    when (val sgv = operationRegex.matchEntire(gv[2])?.groupValues) {
                        null -> Literal(gv[2].toLong())
                        else -> Operation(sgv[1], sgv[3], sgv[2].first())
                    }
                }
            }
    }

    tailrec fun expand(map: Map<String, Instruction>): Map<String, Instruction> {
        val newMap = map.mapValues { (_, instruction) ->
            when (instruction) {
                is Literal -> instruction
                is Operation -> {
                    val left = map[instruction.left] as? Literal
                    val right = map[instruction.right] as? Literal
                    if (left != null && right != null) {
                        when (instruction.operation) {
                            '+' -> Literal(left.value + right.value)
                            '-' -> Literal(left.value - right.value)
                            '*' -> Literal(left.value * right.value)
                            '/' -> Literal(left.value / right.value)
                            else -> error("shouldn't ever get here")
                        }
                    } else {
                        instruction
                    }
                }
                else -> error("shouldn't ever get here.")
            }
        }

        // terminate when we're done expanding
        return if (newMap == map) map else expand(newMap)
    }

    fun part1(input: List<String>): Long {
        val finalMap = expand(input.parse())
        return (finalMap["root"] as? Literal)?.value
            ?: error("didn't expand to 'root'.")
    }

    fun part2(input: List<String>): Long {
        val map = input.parse()

        fun getRootDifference(humanValue: Long): Long {
            val rootOperation = map["root"] as? Operation ?: error("shouldn't get here.")
            val expandedMap = expand(map + ("humn" to Literal(humanValue)))

            // here we go
            val left = expandedMap[rootOperation.left] as? Literal ?: error("bad left operand")
            val right = expandedMap[rootOperation.right] as? Literal ?: error("bad left operand")
            return left.value - right.value
        }

        // fudged newton-raphson
        tailrec fun search(value: Long): Long {
            val leftDifference = getRootDifference(value)
            val rightDifference = getRootDifference(value + 1)

            // termination / fudge conditions
            when {
                leftDifference == 0L -> return value
                rightDifference == 0L -> return value + 1
                rightDifference - leftDifference == 0L -> return search(value + 1)
            }

            // slow newton-raphson (at 2/3rds corrective speed)
            return search(value - ((leftDifference * 2 / (rightDifference - leftDifference)) / 3))
        }

        return search(0L)
    }

    val testInput = readInput("Day21_test")
    check(part1(testInput) == 152L)
    check(part2(testInput) == 301L)

    val input = readInput("Day21")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
