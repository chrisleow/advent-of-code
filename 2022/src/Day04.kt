fun main() {

    val lineRegex = "(\\d+)-(\\d+),(\\d+)-(\\d+)".toRegex()

    fun List<String>.parse() = this
        .mapNotNull { lineRegex.matchEntire(it) }
        .map { match -> match.groupValues }
        .map { gv -> Pair(gv[1].toInt() .. gv[2].toInt(), gv[3].toInt() .. gv[4].toInt()) }
        .toList()

    fun part1(input: List<String>) = input
        .parse()
        .count { (r1, r2) -> (r1.first in r2 && r1.last in r2) || (r2.first in r1 && r2.last in r1) }

    fun part2(input: List<String>) = input
        .parse()
        .count { (r1, r2) -> r1.first in r2 || r1.last in r2 || r2.first in r1 || r2.last in r1 }

    val testInput = readInput("Day04_test")
    check(part1(testInput) == 2)
    check(part2(testInput) == 4)

    val input = readInput("Day04")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
