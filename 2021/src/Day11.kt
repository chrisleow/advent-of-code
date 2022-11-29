fun main() {

    data class Point(val x: Int, val y: Int) {
        val adjacent by lazy {
            listOf(
                Point(x - 1, y - 1),
                Point(x, y - 1),
                Point(x + 1, y - 1),
                Point(x - 1, y),
                Point(x + 1, y),
                Point(x - 1, y + 1),
                Point(x, y + 1),
                Point(x + 1, y + 1),
            )
        }
    }

    fun parseInput(input: List<String>): Map<Point, Int> = buildMap {
        input.filter { it.isNotBlank() }.forEachIndexed { y, line ->
            line.trim().forEachIndexed { x, char ->
                put(Point(x, y), char.toString().toInt())
            }
        }
    }

    fun Map<Point, Int>.print() {
        val maxX = this.keys.maxOf { it.x }
        val maxY = this.keys.maxOf { it.y }
        (0 .. maxY).forEach { y ->
            (0 .. maxX).forEach { x ->
                print(
                    when (val value = this[Point(x, y)]) {
                        null -> " "
                        in (0 .. 9) -> value.toString()
                        else -> "?"
                    }
                )
            }
            println()
        }
        println()
    }

    fun Map<Point, Int>.next(): Map<Point, Int> {
        tailrec fun cascadeFlashes(map: Map<Point, Int>): Map<Point, Int> {
            val flashPoints = map.filter { it.value > 9 }.keys
            return when {
                flashPoints.isEmpty() -> map
                else -> cascadeFlashes(
                    map.mapValues { (point, energy) ->
                        when {
                            energy == 0 -> 0
                            point in flashPoints -> 0
                            else -> energy + (point.adjacent intersect flashPoints).size
                        }
                    }
                )
            }
        }

        return cascadeFlashes( this.mapValues { it.value + 1 })
    }

    fun part1(input: List<String>): Int {
        val initialMap = parseInput(input)
        return generateSequence(initialMap) { map -> map.next() }
            .drop(1)
            .take(100)
            .sumOf { map -> map.count { it.value == 0 } }
    }

    fun part2(input: List<String>): Int {
        val initialMap = parseInput(input)
        return generateSequence(initialMap) { map -> map.next() }
            .indexOfFirst { map -> map.values.all { it == 0 } }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day11_test")
    check(part1(testInput) == 1656)
    check(part2(testInput) == 195)

    val input = readInput("Day11")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
