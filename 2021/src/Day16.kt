sealed class Packet {
    abstract val version: Int
    data class Literal(override val version: Int, val value: Long) : Packet()
    data class Operator(override val version: Int, val typeId: Int, val packets: List<Packet>) : Packet()
}

fun main() {

    fun String.toBinary() = this.asIterable().joinToString("") {
        it.toString().toInt(16).toString(2).padStart(4, '0')
    }
    
    fun parse(hexString: String): Packet {
        val binaryString = hexString.toBinary()

        fun parse(index: Int): Pair<Int, Packet> {
            val version = binaryString.slice(index until index + 3).toInt(2)

            fun parseLiteral(subIndex: Int): Pair<Int, Packet> {
                tailrec fun parseNumbers(index: Int, binaryNumber: String): Pair<Int, String> {
                    val slice = binaryString.slice(index + 1 until index + 5)
                    return when (binaryString[index]) {
                        '0' -> Pair(index + 5, binaryNumber + slice)
                        '1' -> parseNumbers(index + 5, binaryNumber + slice)
                        else -> error("Not a binary value")
                    }
                }
                val (nextIndex, binaryNumber) = parseNumbers(subIndex, "")
                return Pair(nextIndex, Packet.Literal(version, binaryNumber.toLong(2)))
            }

            fun parseOperator(subIndex: Int, typeId: Int): Pair<Int, Packet> {
                return when (val lengthTypeId = binaryString[subIndex]) {
                    '0' -> {
                        val length = binaryString.slice(subIndex + 1 until subIndex + 16).toInt(2)
                        val stopIndex = subIndex + 16 + length
                        tailrec fun parseSubPackets(innerIndex: Int, packets: List<Packet>): List<Packet> {
                            return if (innerIndex >= stopIndex) {
                                packets
                            } else {
                                val (nextSubIndex, packet) = parse(innerIndex)
                                parseSubPackets(nextSubIndex, packets + packet)
                            }
                        }
                        stopIndex to Packet.Operator(version, typeId, parseSubPackets(subIndex + 16, emptyList()))
                    }
                    '1' -> {
                        val count = binaryString.slice(subIndex + 1 until subIndex + 12).toInt(2)
                        val initialState = Pair(subIndex + 12, emptyList<Packet>())
                        val (endIndex, packets) = (0 until count).fold(initialState) { (innerIndex, packets), _ ->
                            val (nextInnerIndex, packet) = parse(innerIndex)
                            nextInnerIndex to (packets + packet)
                        }
                        endIndex to Packet.Operator(version, typeId, packets)
                    }
                    else -> error("Unknown length type ID '$lengthTypeId'.")
                }
            }

            // main parsing logic
            return when (val typeId = binaryString.slice(index + 3 until index + 6).toInt(2)) {
                4 -> parseLiteral(index + 6)
                else -> parseOperator(index + 6, typeId)
            }
        }

        return parse(0).second
    }

    fun Packet.evaluate(): Long {
        return when (this) {
            is Packet.Literal -> this.value
            is Packet.Operator -> when (this.typeId) {
                0 -> this.packets.sumOf { it.evaluate() }
                1 -> this.packets.fold(1L) { acc, p -> acc * p.evaluate() }
                2 -> this.packets.minOf { it.evaluate() }
                3 -> this.packets.maxOf { it.evaluate() }
                5 -> if (this.packets[0].evaluate() > this.packets[1].evaluate()) 1 else 0
                6 -> if (this.packets[0].evaluate() < this.packets[1].evaluate()) 1 else 0
                7 -> if (this.packets[0].evaluate() == this.packets[1].evaluate()) 1 else 0
                else -> error("Unknown type ID '$typeId'.")
            }
        }
    }

    fun part1(input: List<String>): Int {
        fun Packet.sumVersions(): Int = when (this) {
            is Packet.Literal -> version
            is Packet.Operator -> version + packets.sumOf { it.sumVersions() }
        }
        return parse(input.joinToString("").trim()).sumVersions()
    }

    fun part2(input: List<String>): Long {
        val hexString = input.joinToString("").trim()
        return parse(hexString).evaluate()
    }

    val input = readInput("Day16")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
