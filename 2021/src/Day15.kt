import java.util.PriorityQueue
import kotlin.system.measureTimeMillis

fun main() {

    data class Point(val x: Int, val y: Int) : Comparable<Point> {
        val adjacent by lazy {
            listOf(
                Point(x - 1, y),
                Point(x, y - 1),
                Point(x + 1, y),
                Point(x, y + 1),
            )
        }

        override fun compareTo(other: Point): Int {
            return when {
                this.x < other.x -> -1
                this.x > other.x -> 1
                this.y < other.y -> -1
                this.y > other.y -> 1
                else -> 0
            }
        }
    }

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

    fun Map<Point, Int>.printAsCostMap() {
        val maxX = this.keys.maxOf { it.x }
        val maxY = this.keys.maxOf { it.y }
        (0 .. maxY).forEach { y ->
            (0 .. maxX).forEach { x ->
                print(this[Point(x, y)]?.toString()?.padStart(4, ' ') ?: "   ?")
            }
            println()
        }
        println()
    }

    // rewritten to use tail-recursion and purely functional data structures!  Performance, surprisingly, is not
    // far off a hashmap
    fun getLowestCost(risks: Map<Point, Int>): Int {
        val maxX = risks.keys.maxOf { it.x }
        val maxY = risks.keys.maxOf { it.y }

        // A* queue indexed by (priority, point)
        tailrec fun search(queue: AVLPriorityQueue<Pair<Int, Point>>, costs: AVLMap<Point, Int>): Int {
            val (pair, tailQueue) = queue.removeLeft()
            val (_, point) = pair ?: error("not expecting an empty queue")

            // termination condition (end of Dijkstra's algorithm)
            val pointCost = costs[point] ?: error("expecting cost to be present for point")
            if (point == Point(maxX, maxY)) {
                return pointCost
            }

            // calculate updates
            val updates = point.adjacent
                .filter { it.x in (0 .. maxX) && it.y in (0 .. maxY) }
                .mapNotNull { adjacentPoint ->
                    val potentialCost = pointCost + (risks[adjacentPoint] ?: 1_000_000_000)
                    if (potentialCost < (costs[adjacentPoint] ?: 1_000_000_000)) {
                        val priority = potentialCost - (adjacentPoint.x + adjacentPoint.y)
                        Triple(priority, adjacentPoint, potentialCost)
                    } else {
                        null
                    }
                }
            return search(
                costs = costs.addAll(updates.map { (_, point, cost) -> Pair(point, cost) }),
                queue = tailQueue.pushAll(updates.map { (priority, point, _) -> Pair(priority, point) }),
            )
        }

        return search(
            costs = AVLMap<Point, Int>().add(Point(0, 0) to 0),
            queue = AVLPriorityQueue<Pair<Int, Point>>(compareBy({ it.first }, { it.second }))
                .push(0 to Point(0, 0)),
        )
    }

    // Note, I have to break my "functional-only" rule at this point, as performance is a real issue ...
    // Using an A* search algorithm
    fun getLowestCostNonFunctional(risks: Map<Point, Int>): Int {
        val maxX = risks.keys.maxOf { it.x }
        val maxY = risks.keys.maxOf { it.y }

        // A* queue indexed by (priority, point)
        val minCostsSoFar = mutableMapOf(Point(0, 0) to 0)
        val queue = PriorityQueue<Pair<Int, Point>>(compareBy { it.first })
        queue.add(Pair(0, Point(0, 0)))

        // continue until destination achieved
        while (queue.isNotEmpty()) {
            val (_, point) = queue.remove()
            if (point == Point(maxX, maxY)) {
                break
            }

            // examine adjacent points as per A* / Dijkstra
            // note we use (-ManhattenDistance) as our distance heuristic
            val pointCost = minCostsSoFar[point] ?: continue
            point.adjacent
                .filter { it.x in (0 .. maxX) && it.y in (0 .. maxY) }
                .forEach { adjacentPoint ->
                    val potentialCost = pointCost + (risks[adjacentPoint] ?: 1_000_000_000)
                    if (potentialCost < (minCostsSoFar[adjacentPoint] ?: 1_000_000_000)) {
                        val priority = potentialCost - (adjacentPoint.x + adjacentPoint.y)
                        minCostsSoFar[adjacentPoint] = potentialCost
                        queue.add(priority to adjacentPoint)
                    }
                }
        }

        return minCostsSoFar[Point(maxX, maxY)] ?: error("shouldn't get here")
    }

    fun part1(input: List<String>, useFunctional: Boolean): Int = when (useFunctional) {
        true -> getLowestCost(parseInput(input))
        false -> getLowestCostNonFunctional(parseInput(input))
    }

    fun part2(input: List<String>, useFunctional: Boolean): Int = when (useFunctional) {
        true -> getLowestCost(expandMap(parseInput(input)))
        false -> getLowestCostNonFunctional(expandMap(parseInput(input)))
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
    check(part1(testInput, true) == 40)
    check(part2(testInput, true) == 315)

    val input = readInput("Day15")
    println("Part 1: ${part1(input, true)}")
    println("Part 2: ${part2(input, true)}")

    fun timeIt(iterations: Int, callable: () -> Unit): Long {
        callable() // warm-up
        val total = measureTimeMillis { repeat(iterations) { callable() } }
        return total / iterations
    }

    val functionalMs = timeIt(10) { part2(input, true) }
    println("Functional Time Taken (per iteration) for Part 2: ${functionalMs}ms")
    val nonFunctionalMs = timeIt(10) { part2(input, false) }
    println("Non-Functional Time Taken (per iteration) for Part 2: ${nonFunctionalMs}ms")
    println("  Functional is ${(functionalMs - nonFunctionalMs) * 100 / functionalMs}% slower.")
}
