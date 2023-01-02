import kotlin.math.abs

fun main() {

    data class Point2(val x: Int, val y: Int)
    data class Portal(val outPoint: Point2, val outDirection: Char, val inPoint: Point2, val inDirection: Char)
    data class NavState(val point: Point2, val direction: Char)
    data class NavMap(
        val points: Set<Point2>,
        val isWall: (Point2) -> Boolean,
        val portals: Map<NavState, NavState>,
    )

    fun List<String>.parse(): Pair<NavMap, List<String>> {
        val sections = this.split { it.isBlank() }

        val wallPoints = sections[0]
            .flatMapIndexed { y, line ->
                line.mapIndexedNotNull { x, char ->
                    when (char) {
                        '#' -> Point2(x + 1, y + 1) to true
                        '.' -> Point2(x + 1, y + 1) to false
                        else -> null
                    }
                }
            }
            .toMap()

        val instructionRegex = "(\\d+|[UDLR])".toRegex()
        val instructions = instructionRegex
            .findAll(sections[1].joinToString(""))
            .map { it.value }
            .toList()

        val navMap = NavMap(wallPoints.keys, { wallPoints[it] ?: false }, emptyMap())
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

    fun NavMap.toWrapped(): NavMap {
        val minMaxXs = points
            .groupBy { it.y }
            .mapValues { (_, ps) -> ps.minOf { it.x } .. ps.maxOf { it.x } }
        val minMaxYs = points
            .groupBy { it.x }
            .mapValues { (_, ps) -> ps.minOf { it.y } .. ps.maxOf { it.y } }
        return copy(
            portals = run {
                val portals = sequence {
                    minMaxXs.forEach { (y, xRange) ->
                        val (minX, maxX) = Pair(xRange.first, xRange.last)
                        yield(Portal(Point2(minX, y), '<', Point2(maxX, y), '<'))
                        yield(Portal(Point2(maxX, y), '>', Point2(minX, y), '>'))
                    }
                    minMaxYs.forEach { (x, yRange) ->
                        val (minY, maxY) = Pair(yRange.first, yRange.last)
                        yield(Portal(Point2(x, minY), '^', Point2(x, maxY), '^'))
                        yield(Portal(Point2(x, maxY), 'v', Point2(x, minY), 'v'))
                    }
                }
                portals.associate { NavState(it.outPoint, it.outDirection) to NavState(it.inPoint, it.inDirection) }
            }
        )
    }

    fun NavMap.toCube(): NavMap {

        data class Point3(val x: Int, val y: Int, val z: Int)
        data class CornerMapping(val p2: Point2, val p3: Point3)
        data class FaceMapping(
            val topLeft: CornerMapping,
            val topRight: CornerMapping,
            val bottomLeft: CornerMapping,
            val bottomRight: CornerMapping,
        )

        val width = minOf(
            points.groupingBy { it.x }.eachCount().minOf { it.value },
            points.groupingBy { it.y }.eachCount().minOf { it.value },
        )

        fun p2(x: Int, y: Int) = Point2(x, y)
        fun p3(x: Int, y: Int, z: Int) = Point3(x, y, z)
        fun cm(p2: Point2, p3: Point3) = CornerMapping(p2, p3)

        fun FaceMapping.corner2s() = listOf(topLeft, topRight, bottomLeft, bottomRight).map { (p2, _) -> p2 }
        fun FaceMapping.corner3s() = listOf(topLeft, topRight, bottomLeft, bottomRight).map { (_, p3) -> p3 }
        fun Point3.distance(other: Point3) = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)

        val allPoint3s = (0 .. 1).flatMap { x ->
            (0 .. 1).flatMap { y ->
                (0 .. 1).map { z -> Point3(x, y, z) }
            }
        }

        // given a face and an edge, find the adjoining face mappings
        fun getNeighbourMappings(mapping: FaceMapping): List<FaceMapping> {
            fun Point2.up() = copy(y = y - width)
            fun Point2.down() = copy(y = y + width)
            fun Point2.left() = copy(x = x - width)
            fun Point2.right() = copy(x = x + width)

            fun inferCorner(anchor: Point3, disallowed: List<Point3>) =
                allPoint3s.first { it.distance(anchor) == 1 && it !in disallowed }

            // fill out everything we know from translating a face up, down, left or right, we
            // know everything except two corner points in 3D, which we can infer
            val (tl2, tl3) = mapping.topLeft
            val (tr2, tr3) = mapping.topRight
            val (bl2, bl3) = mapping.bottomLeft
            val (br2, br3) = mapping.bottomRight
            val disallowed = mapping.corner3s()

            return listOf(
                FaceMapping(
                    topLeft = cm(tl2.up(), inferCorner(tl3, disallowed)),
                    topRight = cm(tr2.up(), inferCorner(tr3, disallowed)),
                    bottomLeft = cm(bl2.up(), tl3),
                    bottomRight = cm(br2.up(), tr3),
                ),
                FaceMapping(
                    topLeft = cm(tl2.down(), bl3),
                    topRight = cm(tr2.down(), br3),
                    bottomLeft = cm(bl2.down(), inferCorner(bl3, disallowed)),
                    bottomRight = cm(br2.down(), inferCorner(br3, disallowed)),
                ),
                FaceMapping(
                    topLeft = cm(tl2.left(), inferCorner(tl3, disallowed)),
                    topRight = cm(tr2.left(), tl3),
                    bottomLeft = cm(bl2.left(), inferCorner(bl3, disallowed)),
                    bottomRight = cm(br2.left(), bl3),
                ),
                FaceMapping(
                    topLeft = cm(tl2.right(), tr3),
                    topRight = cm(tr2.right(), inferCorner(tr3, disallowed)),
                    bottomLeft = cm(bl2.right(), br3),
                    bottomRight = cm(br2.right(), inferCorner(br3, disallowed)),
                ),
            )
        }

        tailrec fun getFaceMappings(mappings: List<FaceMapping>): List<FaceMapping> {
            val newMappings = mappings
                .flatMap { mapping -> getNeighbourMappings(mapping) }
                .filter { mapping -> mapping.corner2s().all { it in points } }
                .filter { mapping -> mapping !in mappings }
            return if (newMappings.isEmpty()) {
                mappings
            } else {
                getFaceMappings(mappings + newMappings)
            }
        }

        // fix one face, infer all other mappings
        val mappings = getFaceMappings(
            run {
                val c2 = points.minBy { (it.y * 10000) + it.x }
                listOf(
                    FaceMapping(
                        topLeft = cm(c2, p3(0, 0, 0)),
                        topRight = cm(c2.copy(x = c2.x + width - 1), p3(1, 0, 0)),
                        bottomLeft = cm(c2.copy(y = c2.y + width - 1), p3(0, 1, 0)),
                        bottomRight = cm(c2.copy(x = c2.x + width - 1, y = c2.y + width - 1), p3(1, 1, 0)),
                    )
                )
            }
        )

        // find correlating edges, in both directions for ease of mapping / filtering
        val bidirectionalEdgePairs = mappings
            .flatMap {
                listOf(
                    it.topLeft to it.topRight,
                    it.topLeft to it.bottomLeft,
                    it.topRight to it.topLeft,
                    it.topRight to it.bottomRight,
                    it.bottomLeft to it.topLeft,
                    it.bottomLeft to it.bottomRight,
                    it.bottomRight to it.bottomLeft,
                    it.bottomRight to it.topRight,
                )
            }
            .groupBy { (cm0, cm1) -> cm0.p3 to cm1.p3 }
            .values

        fun getLinePoints(p0: Point2, p1: Point2): List<Point2> {
            val dx = maxOf(minOf(p1.x - p0.x, 1), -1)
            val dy = maxOf(minOf(p1.y - p0.y, 1), -1)
            val pointsUpToP1 = generateSequence(p0) { Point2(it.x + dx, it.y + dy) }
                .takeWhile { it != p1 }
                .toList()
            return pointsUpToP1 + p1
        }

        fun getDirectionInOutPair(point: Point2): Pair<Char, Char>? {
            return when {
                point.copy(x = point.x - 1) !in points -> '<' to '>'
                point.copy(x = point.x + 1) !in points -> '>' to '<'
                point.copy(y = point.y - 1) !in points -> '^' to 'v'
                point.copy(y = point.y + 1) !in points -> 'v' to '^'
                else -> null
            }
        }

        // we now have faces mapped to edges on a cube, to infer where the "portals" are
        return copy(
            portals = bidirectionalEdgePairs
                .flatMap { edgePairs ->
                    val (p0, p1) = edgePairs[0].first.p2 to edgePairs[0].second.p2
                    val (p2, p3) = edgePairs[1].first.p2 to edgePairs[1].second.p2
                    sequence {
                        val pm0 = p2((p0.x + p1.x) / 2, (p0.y + p1.y) / 2)
                        val pm1 = p2((p2.x + p3.x) / 2, (p2.y + p3.y) / 2)
                        val directions0 = getDirectionInOutPair(pm0)
                        val directions1 = getDirectionInOutPair(pm1)

                        // map these line connections to "portals"
                        if (directions0 != null && directions1 != null) {
                            val (direction0out, direction0in) = directions0
                            val (direction1out, direction1in) = directions1
                            getLinePoints(p0, p1)
                                .zip(getLinePoints(p2, p3))
                                .forEach { (sp0, sp1) ->
                                    yield(Portal(sp0, direction0out, sp1, direction1in))
                                    yield(Portal(sp1, direction1out, sp0, direction0in))
                                }
                        }
                    }
                }
                .distinct()
                .associate { NavState(it.outPoint, it.outDirection) to NavState(it.inPoint, it.inDirection) }
        )
    }

    fun navigate(map: NavMap, instructions: List<String>): NavState {
        val initialState = NavState(
            point = map.points.minBy { (it.y * 1000) + it.x },
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
                    val linePoints = generateSequence(state) { (point, direction) ->
                        when (val nextPortalState = map.portals[NavState(point, direction)]) {
                            null -> when (direction) {
                                '^' -> NavState(point.copy(y = point.y - 1), direction)
                                'v' -> NavState(point.copy(y = point.y + 1), direction)
                                '<' -> NavState(point.copy(x = point.x - 1), direction)
                                '>' -> NavState(point.copy(x = point.x + 1), direction)
                                else -> error("not a valid direction")
                            }
                            else -> nextPortalState
                        }
                    }
                    linePoints
                        .take(instruction.toInt() + 1)
                        .takeWhile { (point, _) -> !map.isWall(point) }
                        .last()
                }
            }
        }
    }

    fun part1(input: List<String>): Int {
        val (map, instructions) = input.parse()
        val state = navigate(map.toWrapped(), instructions)
        return (state.point.y * 1000) +
                (state.point.x * 4) +
                (directionScores[state.direction] ?: 0)
    }

    fun part2(input: List<String>): Int {
        val (map, instructions) = input.parse()
        val state = navigate(map.toCube(), instructions)
        return (state.point.y * 1000) +
                (state.point.x * 4) +
                (directionScores[state.direction] ?: 0)
    }

    val testInput = readInput("Day22_test")
    check(part1(testInput) == 6032)
    check(part2(testInput) == 5031)

    val input = readInput("Day22")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
