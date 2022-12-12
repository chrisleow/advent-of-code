fun main() {

    data class Point(val x: Int, val y: Int)

    fun List<String>.parse() = this
        .joinToString("")
        .trim()
        .toInt()

    fun getPowerLevel(x: Int, y: Int, serialNumber: Int) =
        (((((x + 10) * y) + serialNumber) * (x + 10) / 100) % 10) - 5

    fun getSummedAreaMap(serialNumber: Int): Map<Point, Int> {
        val powerLevels = (1 .. 301)
            .flatMap { x -> (1 .. 301).map { y -> Point(x, y) to getPowerLevel(x, y, serialNumber)} }
            .toMap()

        return buildMap {
            (1 .. 301).forEach { y ->
                (1 .. 301).forEach { x ->
                    fun areaAt(x0: Int, y0: Int) = get(Point(x0, y0)) ?: 0
                    val components = listOf(
                        areaAt(x, y - 1),
                        areaAt(x - 1, y) - areaAt(x - 1, y - 1),
                        powerLevels[Point(x, y)] ?: 0,
                    )
                    put(Point(x, y), components.sum())
                }
            }
        }
    }

    fun getArea(summedAreaMap: Map<Point, Int>, x: Int, y: Int, width: Int): Int {
        return ((summedAreaMap[Point(x + width - 1, y + width - 1)] ?: 0)
                - (summedAreaMap[Point(x - 1, y + width - 1)] ?: 0)
                - (summedAreaMap[Point(x + width - 1, y - 1)] ?: 0)
                + (summedAreaMap[Point(x - 1, y - 1)] ?: 0))
    }

    fun <V, U> Iterable<V>.crossWith(other: Iterable<U>) =
        this.flatMap { v -> other.map { u -> Pair(v, u) } }

    fun part1(input: List<String>): String {
        val serialNumber = input.parse()
        val summedAreaMap = getSummedAreaMap(serialNumber)
        return (1 .. 299).crossWith(1 .. 299)
            .maxBy { (topX, topY) -> getArea(summedAreaMap, topX, topY, 3) }
            .let { (x, y) -> "$x,$y" }
    }

    fun part2(input: List<String>): String {
        val serialNumber = input.parse()
        val summedAreaMap = getSummedAreaMap(serialNumber)
        return (1 .. 300)
            .flatMap { width ->
                (1 .. 301 - width).crossWith(1 .. 301 - width)
                    .map { (x, y) -> Point(x, y) to width }
            }
            .maxBy { (point, width) -> getArea(summedAreaMap, point.x, point.y, width) }
            .let { (point, width) -> "${point.x},${point.y},${width}" }
    }

    val testInput = readInput("Day_2018_11_test")
    check(part1(testInput) == "21,61")
    check(part2(testInput) == "232,251,12")

    val input = readInput("Day_2018_11")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}