fun main() {

    fun List<String>.parse() = this
        .filter { it.isNotBlank() }
        .map { it.trim() }

    fun readSNAFU(line: String): Long {
        return generateSequence(1L) { it * 5 }
            .zip(line.reversed().asSequence())
            .sumOf { (base, char) ->
                when (char) {
                    '2' -> base * 2
                    '1' -> base
                    '0' -> 0
                    '-' -> base * -1
                    '=' -> base * -2
                    else -> error("shouldn't get here")
                }
            }
    }

    fun writeSNAFU(number: Long): String {
        val base5Digits = number
            .toString(radix = 5)
            .map { it.toString().toInt() }

        tailrec fun produce(digits: List<Int>, carry: Int = 0, suffix: String = ""): String {
            if (digits.isEmpty() && carry == 0) {
                return suffix
            }

            // continuation case
            return when ((digits.lastOrNull() ?: 0) + carry) {
                0 -> produce(digits.dropLast(1), 0, "0$suffix")
                1 -> produce(digits.dropLast(1), 0, "1$suffix")
                2 -> produce(digits.dropLast(1), 0, "2$suffix")
                3 -> produce(digits.dropLast(1), 1, "=$suffix")
                4 -> produce(digits.dropLast(1), 1, "-$suffix")
                5 -> produce(digits.dropLast(1), 1, "0$suffix")
                else -> error("shouldn't get here")
            }
        }

        return produce(base5Digits)
    }

    fun part1(input: List<String>): String {
        return writeSNAFU(input.parse().sumOf { readSNAFU(it) })
    }

    val testInput = readInput("Day25_test")
    check(part1(testInput) == "2=-1=0")

    val input = readInput("Day25")
    println("Part 1: ${part1(input)}")
}
