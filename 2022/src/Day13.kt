fun main() {

    abstract class Packet
    data class SinglePacket(val value: Int) : Packet()
    data class ListPacket(val packets: List<Packet>) : Packet()

    val tokenRegex = "(\\[|]|\\d+)".toRegex()

    fun List<String>.parse(): List<List<Packet>> = this
        .split { it.isBlank() }
        .map { lines ->
            lines.map { line ->
                val tokens = tokenRegex.findAll(line).map { it.value }.toList()

                fun parse(index: Int): Pair<Int, Packet> {
                    tailrec fun parseMany(subIndex: Int, packets: List<Packet>): Pair<Int, Packet> {
                        return if (tokens[subIndex] == "]") {
                            Pair(subIndex + 1, ListPacket(packets))
                        } else {
                            val (nextSubIndex, packet) = parse(subIndex)
                            parseMany(nextSubIndex, packets + packet)
                        }
                    }

                    return if (tokens[index] == "[") {
                        parseMany(index + 1, emptyList())
                    } else {
                        Pair(index + 1, SinglePacket(tokens[index].toInt()))
                    }
                }

                parse(0).second
            }
        }

    fun Packet.toCanonicalString(): String = when (this) {
        is SinglePacket -> {
            this.value.toString()
        }
        is ListPacket -> {
            this.packets.joinToString(",", prefix = "[", postfix = "]") { it.toCanonicalString() }
        }
        else -> error("wouldn't get here with embedded sealed classes!")
    }

    fun comparePackets(left: Packet, right: Packet): Int =
        when {
            left is ListPacket && right is ListPacket -> {
                val firstComparison = (0 until maxOf(left.packets.size, right.packets.size))
                    .asSequence()
                    .map { index ->
                        val subLeft = left.packets.getOrNull(index) ?: return@map -1
                        val subRight = right.packets.getOrNull(index) ?: return@map 1
                        comparePackets(subLeft, subRight)
                    }
                    .firstOrNull { it != 0 }
                (firstComparison?: 0)
            }
            left is SinglePacket -> when (right) {
                is SinglePacket -> left.value.compareTo(right.value)
                else -> comparePackets(ListPacket(listOf(left)), right)
            }
            right is SinglePacket -> comparePackets(left, ListPacket(listOf(right)))
            else -> error("wouldn't get here with embedded sealed classes!")
        }

    fun part1(input: List<String>) = input
        .parse()
        .withIndex()
        .sumOf { (index, packets) -> if (comparePackets(packets[0], packets[1]) == -1) index + 1 else 0 }

    fun part2(input: List<String>) = (input + listOf("[[2]]", "[[6]]"))
        .parse()
        .asSequence()
        .flatten()
        .sortedWith { left, right -> comparePackets(left, right) }
        .withIndex()
        .filter { (_, packet) -> packet.toCanonicalString() in listOf("[[2]]", "[[6]]") }
        .map { (zeroIndex, _) -> zeroIndex + 1 }
        .reduce { index0, index1 -> index0 * index1 }

    val testInput = readInput("Day13_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 140)

    val input = readInput("Day13")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
