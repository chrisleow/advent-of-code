import java.util.PriorityQueue

fun main() {

    data class Point(val x: Int, val y: Int)

    fun Point.getAdjacent() = listOf(
        Point(this.x - 1, this.y),
        Point(this.x, this.y - 1),
        Point(this.x + 1, this.y),
        Point(this.x, this.y + 1),
    )

    fun parseInput(input: List<String>): Map<Point, Int> = buildMap {
        input.filter { it.isNotBlank() }.forEachIndexed { y, line ->
            line.trim().forEachIndexed { x, char ->
                put(Point(x, y), char.toString().toInt())
            }
        }
    }

    fun expandMap(risks: Map<Point, Int>): Map<Point, Int> = buildMap {
        val maxX = risks.keys.maxOf { it.x }
        val maxY = risks.keys.maxOf { it.y }
        (0 .. 4).forEach { tileX ->
            (0 .. 4).forEach { tileY ->
                (0..maxX).forEach { dx ->
                    (0..maxY).forEach { dy ->
                        val point = Point(tileX * (maxX + 1) + dx, tileY * (maxY + 1) + dy)
                        val risk = when (val originalRisk = risks[Point(dx, dy)]) {
                            null -> error("should never get here")
                            else -> ((originalRisk + tileX + tileY - 1) % 9) + 1
                        }
                        put(point, risk)
                    }
                }
            }
        }
    }

    fun getLowestCost(risks: Map<Point, Int>): Int {
        val maxX = risks.keys.maxOf { it.x }
        val maxY = risks.keys.maxOf { it.y }

        // A* queue indexed by (priority, point)
        val minCostsSoFar = mutableMapOf(Point(0, 0) to 0)
        val queue = PriorityQueue<Pair<Int, Point>>(compareBy { it.first })
        queue.add(0 to Point(0, 0))

        // continue until destination achieved
        while (queue.isNotEmpty()) {
            val (cost, point) = queue.remove()
            if (point == Point(maxX, maxY)) {
                return cost
            }

            // examine adjacent points as per Dijkstra
            // note we use Manhatten Distance as our distance heuristic
            val pointCost = minCostsSoFar[point] ?: continue
            point
                .getAdjacent()
                .filter { it.x in (0 .. maxX) && it.y in (0 .. maxY) }
                .forEach { adjacentPoint ->
                    val adjacentCost = pointCost + (risks[adjacentPoint] ?: Int.MAX_VALUE)
                    if (adjacentCost < (minCostsSoFar[adjacentPoint] ?: Int.MAX_VALUE)) {
                        minCostsSoFar[adjacentPoint] = adjacentCost
                        queue.add(adjacentCost to adjacentPoint)
                    }
                }
        }

        error("Shouldn't get here.")
    }

    fun part1(input: List<String>): Int = getLowestCost(parseInput(input))
    fun part2(input: List<String>): Int = getLowestCost(expandMap(parseInput(input)))

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
    check(part1(testInput) == 40)
    check(part2(testInput) == 315)

    val input = readInput("Day15")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
