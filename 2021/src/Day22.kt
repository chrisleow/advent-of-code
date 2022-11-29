data class Cuboid(val xRange: IntRange, val yRange: IntRange, val zRange: IntRange) {
    fun isEmpty() = (xRange.isEmpty() || yRange.isEmpty() || zRange.isEmpty())
    val volume = sequenceOf(xRange, yRange, zRange)
        .fold(1L) { acc, range -> acc * maxOf(range.last - range.first + 1, 0) }
}

sealed class ReactorInstruction {
    abstract val cuboid: Cuboid
    data class On(override val cuboid: Cuboid) : ReactorInstruction()
    data class Off(override val cuboid: Cuboid) : ReactorInstruction()
}

fun main() {

    fun parseInput(input: List<String>): List<ReactorInstruction> {
        val regex = "(on|off) x=(-?\\d+)\\.\\.(-?\\d+),y=(-?\\d+)\\.\\.(-?\\d+),z=(-?\\d+)\\.\\.(-?\\d+)".toRegex()
        return input
            .mapNotNull { regex.matchEntire(it.trim())?.groupValues }
            .map { gv ->
                val cuboid = Cuboid(
                    xRange = (gv[2].toInt() .. gv[3].toInt()),
                    yRange = (gv[4].toInt() .. gv[5].toInt()),
                    zRange = (gv[6].toInt() .. gv[7].toInt()),
                )
                if (gv[1] == "on") ReactorInstruction.On(cuboid) else ReactorInstruction.Off(cuboid)
            }
    }

    infix fun IntRange.intersect(other: IntRange) =
        (maxOf(this.first, other.first) .. minOf(this.last, other.last))

    infix fun Cuboid.intersect(other: Cuboid) = Cuboid(
        xRange = this.xRange intersect other.xRange,
        yRange = this.yRange intersect other.yRange,
        zRange = this.zRange intersect other.zRange,
    )

    // lantern cuboids!
    fun Map<Cuboid, Int>.apply(instruction: ReactorInstruction): Map<Cuboid, Int> {
        val newCuboidCounts = this.entries
            .asSequence()
            .map { (cuboid, count) -> (cuboid intersect instruction.cuboid) to -count }
            .plus(if (instruction is ReactorInstruction.On) listOf(instruction.cuboid to 1) else emptyList())
        return (this.entries.asSequence().map { (c, n) -> c to n } + newCuboidCounts)
            .groupBy({ it.first }) { it.second }
            .mapValues { (_, counts) -> counts.sum() }
            .filter { (cuboid, count) -> !cuboid.isEmpty() && count != 0 }
    }

    fun List<ReactorInstruction>.calculateFinalVolume(): Long {
        return this
            .fold(emptyMap<Cuboid, Int>()) { state, instruction -> state.apply(instruction) }
            .asIterable()
            .sumOf { (cuboid, count) -> cuboid.volume * count }
    }

    fun part1(input: List<String>): Long {
        return parseInput(input).takeWhile { it.cuboid.volume < 100 * 100 * 100 }.calculateFinalVolume()
    }

    fun part2(input: List<String>): Long {
        return parseInput(input).calculateFinalVolume()
    }

    // test
    val testInput = readInput("Day22_test")
    check(part1(testInput) == 474140L)
    check(part2(testInput) == 2758514936282235L)

    val input = readInput("Day22")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
