sealed class Packet {
    data class Single(val value: Int) : Packet()
    data class Group(val packets: List<Packet>) : Packet()
}

fun main() {

    fun List<String>.parse(): List<List<Packet>> {
        val tokenRegex = "(\\[|]|\\d+)".toRegex()
        return this
            .split { it.isBlank() }
            .map { lines ->
                lines.map { line ->
                    val tokens = tokenRegex.findAll(line).map { it.value }.toList()
                    fun parse(index: Int): Pair<Int, Packet> =
                        if (tokens[index] != "[") {
                            Pair(index + 1, Packet.Single(tokens[index].toInt()))
                        } else {
                            tailrec fun parseGroup(subIndex: Int, subPackets: List<Packet>): Pair<Int, Packet> =
                                if (tokens[subIndex] == "]") {
                                    Pair(subIndex + 1, Packet.Group(subPackets))
                                } else {
                                    val (nextSubIndex, subPacket) = parse(subIndex)
                                    parseGroup(nextSubIndex, subPackets + subPacket)
                                }
                            parseGroup(index + 1, emptyList())
                        }
                    parse(0).second
                }
            }
    }

    fun Packet.toCanonicalString(): String = when (this) {
        is Packet.Single -> this.value.toString()
        is Packet.Group -> this.packets.joinToString(", ", prefix = "[", postfix = "]") { subPacket ->
            subPacket.toCanonicalString()
        }
    }

    fun comparePackets(left: Packet, right: Packet): Int {
        when {
            left is Packet.Single && right is Packet.Single -> {
                return when {
                    left.value < right.value -> -1
                    left.value > right.value -> 1
                    else -> 0
                }
            }
            left is Packet.Group && right is Packet.Group -> {
                (0 until maxOf(left.packets.size, right.packets.size)).forEach { index ->
                    val subLeft = if (index < left.packets.size) left.packets[index] else return -1
                    val subRight = if (index < right.packets.size) right.packets[index] else return 1
                    when (val comparison = comparePackets(subLeft, subRight)) {
                        0 -> { /** noop */ }
                        else -> return comparison
                    }
                }
                return 0
            }
            else -> {
                return when {
                    left is Packet.Single -> comparePackets(Packet.Group(listOf(left)), right)
                    right is Packet.Single -> comparePackets(left, Packet.Group(listOf(right)))
                    else -> error("should never et here.")
                }
            }
        }
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
        .reduce { a, b -> a * b }

    val testInput = readInput("Day13_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 140)

    val input = readInput("Day13")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
