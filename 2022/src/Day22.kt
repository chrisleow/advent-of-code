//fun main() {
//
//    data class Point(val x: Int, val y: Int)
//    data class NavMap(val wallPoints: Map<Point, Boolean>, val directionNeighbours: Map<Point, Map<Char, Point>>)
//    data class NavState(val point: Point, val direction: Char)
//
//    fun List<String>.parse(): Pair<NavMap, List<String>> {
//        val sections = this.split { it.isBlank() }
//
//        val points = sections[0]
//            .flatMapIndexed { y, line ->
//                line.mapIndexedNotNull { x, char ->
//                    when (char) {
//                        '#' -> Point(x + 1, y + 1) to true
//                        '.' -> Point(x + 1, y + 1) to false
//                        else -> null
//                    }
//                }
//            }
//            .toMap()
//
//        val instructionRegex = "(\\d+|[UDLR])".toRegex()
//        val instructions = instructionRegex
//            .findAll(sections[1].joinToString(""))
//            .map { it.value }
//            .toList()
//
//        val navMap = NavMap(
//            wallPoints = points,
//            directionNeighbours = points.keys.associateWith { point ->
//                val allNeighbours = mapOf(
//                    '<' to Point(point.x - 1, point.y),
//                    '>' to Point(point.x + 1, point.y),
//                    '^' to Point(point.x, point.y - 1),
//                    'v' to Point(point.x, point.y + 1),
//                )
//                allNeighbours.filter { it.value in points }
//            },
//        )
//        return Pair(navMap, instructions)
//    }
//
//    val directionChanges = mapOf(
//        '^' to mapOf("L" to '<', "R" to '>'),
//        'v' to mapOf("L" to '>', "R" to '<'),
//        '<' to mapOf("L" to 'v', "R" to '^'),
//        '>' to mapOf("L" to '^', "R" to 'v'),
//    )
//
//    val directionScores = mapOf(
//        '^' to 3,
//        'v' to 1,
//        '<' to 2,
//        '>' to 0,
//    )
//
//    fun Point.getNeighbours(includeCorners: Boolean = false) = sequence {
//        yield(Point(x - 1, y))
//        yield(Point(x + 1, y))
//        yield(Point(x, y - 1))
//        yield(Point(x, y + 1))
//        if (includeCorners) {
//            yield(Point(x - 1, y - 1))
//            yield(Point(x - 1, y + 1))
//            yield(Point(x + 1, y - 1))
//            yield(Point(x + 1, y + 1))
//        }
//    }
//
//    fun NavMap.toWrapped(): NavMap {
//        val points = wallPoints.keys
//        val minXs = points.groupBy { it.y }.mapValues { (_, ps) -> ps.minOf { it.x } }
//        val maxXs = points.groupBy { it.y }.mapValues { (_, ps) -> ps.maxOf { it.x } }
//        val minYs = points.groupBy { it.x }.mapValues { (_, ps) -> ps.minOf { it.y } }
//        val maxYs = points.groupBy { it.x }.mapValues { (_, ps) -> ps.maxOf { it.y } }
//        return copy(
//            directionNeighbours = points.associateWith { point ->
//                mapOf(
//                    '^' to (directionNeighbours[point]?.get('^') ?: Point(point.x, maxYs[point.x]!!)),
//                    'v' to (directionNeighbours[point]?.get('v') ?: Point(point.x, minYs[point.x]!!)),
//                    '<' to (directionNeighbours[point]?.get('<') ?: Point(maxXs[point.y]!!, point.y)),
//                    '>' to (directionNeighbours[point]?.get('>') ?: Point(minXs[point.y]!!, point.y)),
//                )
//            },
//        )
//    }
//
//    fun NavMap.toCube(): NavMap {
//
//        // identify cube faces
//        val width = minOf(
//            this.wallPoints.keys.groupingBy { it.x }.eachCount().minOf { it.value },
//            this.wallPoints.keys.groupingBy { it.y }.eachCount().maxOf { it.value },
//        )
//
//        val zipLineDirections = when (width) {
//            4 -> {
//
//            }
//            50 -> {
//
//            }
//            else -> error("Unrecognised input")
//        }
//
//        val cornerPoint = this.wallPoints.keys.minBy { (it.x * 1000) + it.y }
//        val cubeMap = search(mapOf(cornerPoint to Point3(1, 1, 0)))
//        return this
//    }
//
//    fun navigate(map: NavMap, instructions: List<String>): NavState {
//        val initialState = NavState(
//            point = map.wallPoints.keys.minBy { (it.y * 1000) + it.x },
//            direction = '>',
//        )
//
//        return instructions.fold(initialState) { state, instruction ->
//            when (instruction) {
//                "L", "R" -> {
//                    val newDirection = directionChanges[state.direction]
//                        ?.get(instruction)
//                        ?: error("shouldn't get here.")
//                    state.copy(direction = newDirection)
//                }
//                else -> {
//                    val lastPoint = generateSequence(state.point) { p -> map.directionNeighbours[p]?.get(state.direction) }
//                        .take(instruction.toInt() + 1)
//                        .takeWhile { p -> map.wallPoints[p] != true }
//                        .last()
//                    state.copy(point = lastPoint)
//                }
//            }
//        }
//    }
//
//    fun part1(input: List<String>): Int {
//        val (map, instructions) = input.parse()
//        val state = navigate(map.toWrapped(), instructions)
//        return (state.point.y * 1000) +
//                (state.point.x * 4) +
//                (directionScores[state.direction] ?: 0)
//    }
//
//    fun part2(input: List<String>): Int {
//        val (map, instructions) = input.parse()
//        val state = navigate(map.toCube(), instructions)
//        return (state.point.y * 1000) +
//                (state.point.x * 4) +
//                (directionScores[state.direction] ?: 0)
//    }
//
//    val testInput = readInput("Day22_test")
//    check(part1(testInput) == 6032)
//    check(part2(testInput) == 5031)
//
//    val input = readInput("Day22")
//    println("Part 1: ${part1(input)}")
//    println("Part 2: ${part2(input)}")
//}
