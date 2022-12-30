fun main() {

    data class Point(val x: Int, val y: Int)
    data class NavMap(val wallPoints: Map<Point, Boolean>, val neighbours: Map<Point, Map<Char, Point>>)
    data class NavState(val point: Point, val direction: Char)

    fun List<String>.parse(): Pair<NavMap, List<String>> {
        val sections = this.split { it.isBlank() }

        val points = sections[0]
            .flatMapIndexed { y, line ->
                line.mapIndexedNotNull { x, char ->
                    when (char) {
                        '#' -> Point(x + 1, y + 1) to true
                        '.' -> Point(x + 1, y + 1) to false
                        else -> null
                    }
                }
            }
            .toMap()

        fun moveOne(x: Int, y: Int, dx: Int, dy: Int): Point {
            val xn = x + dx
            val yn = y + dy
            return Point(
                x = when {
                    Point(xn, y) in points -> xn
                    dx < 0 -> points.keys.filter { it.y == y }.maxOf { it.x }
                    dx > 0 -> points.keys.filter { it.y == y }.minOf { it.x }
                    else -> error("shouldn't get here.")
                },
                y = when {
                    Point(x, yn) in points -> yn
                    dy < 0 -> points.keys.filter { it.x == x }.maxOf { it.y }
                    dy > 0 -> points.keys.filter { it.x == x }.minOf { it.y }
                    else -> error("shouldn't get here.")
                }
            )
        }

        val instructionRegex = "(\\d+|[UDLR])".toRegex()
        val instructions = instructionRegex
            .findAll(sections[1].joinToString(""))
            .map { it.value }
            .toList()

        val navMap = NavMap(
            wallPoints = points,
            neighbours = points.mapValues { (point, _) ->
                mapOf(
                    '^' to moveOne(point.x, point.y, 0, -1),
                    'v' to moveOne(point.x, point.y, 0, 1),
                    '<' to moveOne(point.x, point.y, -1, 0),
                    '>' to moveOne(point.x, point.y, 1, 0),
                )
            },
        )

        return Pair(navMap, instructions)
    }

    val directionChanges = mapOf(
        '^' to mapOf("L" to '<', "R" to '>'),
        'v' to mapOf("L" to '>', "R" to '<'),
        '<' to mapOf("L" to 'v', "R" to '^'),
        '>' to mapOf("L" to '^', "R" to 'v'),
    )

    val directionScores = mapOf(
        '^' to 3,
        'v' to 1,
        '<' to 2,
        '>' to 0,
    )

    fun Point.getAdjacent() = listOf(
        Point(x - 1, y),
        Point(x + 1, y),
        Point(x, y - 1),
        Point(x, y + 1),
    )

//    fun NavMap.toWrapped(): NavMap {
//
//    }
//
//    fun NavMap.toCube(): NavMap {
//
//        // walk the edge from the first convex corner to find neighbours to zip together
//        val initialConcavePoint = wallPoints.keys.first { p ->
//            Point(p.x - 1, p.y - 1) !in wallPoints &&
//            Point(p.x - 1, p.y + 1) in wallPoints &&
//            Point(p.x + 1, p.y - 1) in wallPoints &&
//            Point(p.x + 1, p.y + 1) in wallPoints
//        }
//
//        tailrec fun zip(map: NavMap, points: List<Point>): NavMap {
//
//            // find the two neighbours of these points with no neighbours
//        }
//
//        // identify a corner point and "walk" the edge
//
//        return this
//    }

    fun navigate(map: NavMap, instructions: List<String>): NavState {
        val initialState = NavState(
            point = map.wallPoints.keys.minBy { (it.y * 1000) + it.x },
            direction = '>',
        )

        return instructions.fold(initialState) { state, instruction ->
            when (instruction) {
                "L", "R" -> {
                    val newDirection = directionChanges[state.direction]
                        ?.get(instruction)
                        ?: error("shouldn't get here.")
                    state.copy(direction = newDirection)
                }
                else -> {
                    val lastPoint = generateSequence(state.point) { p -> map.neighbours[p]?.get(state.direction) }
                        .take(instruction.toInt() + 1)
                        .takeWhile { p -> map.wallPoints[p] != true }
                        .last()
                    state.copy(point = lastPoint)
                }
            }
        }
    }

    fun part1(input: List<String>): Int {
        val (map, instructions) = input.parse()
        val state = navigate(map, instructions)
        return (state.point.y * 1000) +
                (state.point.x * 4) +
                (directionScores[state.direction] ?: 0)
    }

    fun part2(input: List<String>): Int {
        val (map, instructions) = input.parse()
        // println(input.parse().toCube())
        val state = navigate(map, instructions)
        return (state.point.y * 1000) +
                (state.point.x * 4) +
                (directionScores[state.direction] ?: 0)
    }

    val testInput = readInput("Day22_test")
    check(part1(testInput) == 6032)
    // check(part2(testInput) == 5031)

    val input = readInput("Day22")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
