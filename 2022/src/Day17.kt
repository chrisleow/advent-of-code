fun main() {

    data class Point(val x: Int, val y: Long)
    data class State(val top: Set<Point>, val jets: String, val jetIndex: Int, val shapeIndex: Int, val count: Int)
    data class StateSignature(val top: Set<Point>, val jetIndex: Int, val shapeIndex: Int)

    fun List<String>.parse() = State(
        top = (0..6).map { Point(it, 0) }.toSet(),
        jets = this.joinToString("").filter { it in "<>" },
        jetIndex = 0,
        shapeIndex = 0,
        count = 0
    )

    val shapes = run {
        val templates = listOf(
            """
                ####
            """.trimIndent(),
            """
                .#.
                ###
                .#.
            """.trimIndent(),
            """
                ..#
                ..#
                ###
            """.trimIndent(),
            """
                #
                #
                #
                #
            """.trimIndent(),
            """
                ##
                ##
            """.trimIndent(),
        )

        templates.map { template ->
            val points = template
                .split("\n")
                .flatMapIndexed { minusY, line ->
                    line.mapIndexedNotNull { x, char -> if (char == '#') Point(x, -minusY.toLong()) else null }
                }

            val minX = points.minOf { it.x }
            val minY = points.minOf { it.y }
            points.map { (x, y) -> Point(x - minX, y - minY) }
        }
    }

    fun State.next(): State {
        fun List<Point>.anyCollision() = this.any { point -> point.x < 0 || point.x > 6 || point in top }

        tailrec fun getRestingPlace(points: List<Point>, currentJetIndex: Int): Pair<List<Point>, Int> {
            val nextJetIndex = (currentJetIndex + 1) % jets.length

            // apply jet
            val shiftedPoints = when (jets[currentJetIndex]) {
                '<' -> points.map { p -> p.copy(x = p.x - 1) }
                '>' -> points.map { p -> p.copy(x = p.x + 1) }
                else -> error("should not get here")
            }
            val maybeShiftedPoints = if (shiftedPoints.anyCollision()) points else shiftedPoints

            // drop one
            val droppedPoints = maybeShiftedPoints.map { p -> p.copy(y = p.y - 1) }
            if (droppedPoints.anyCollision()) {
                return maybeShiftedPoints to nextJetIndex
            }

            return getRestingPlace(droppedPoints, nextJetIndex)
        }

        val startHeight = top.maxOf { it.y } + 4
        val startRockPoints = shapes[shapeIndex].map { p -> Point(p.x + 2, p.y + startHeight) }
        val (restingPoints, newJetsIndex) = getRestingPlace(startRockPoints, jetIndex)

        return this.copy(
            top = run {
                val newTop = top + restingPoints
                val floor = newTop.maxOf { it.y } - 100
                newTop.filter { it.y >= floor }.toSet()
            },
            jetIndex = newJetsIndex,
            shapeIndex = (shapeIndex + 1) % shapes.size,
            count = count + 1,
        )
    }

    fun State.toSignature() = StateSignature(
        top = run {
            val minY = top.minOf { it.y }
            top.map { it.copy(y = it.y - minY) }.toSet()
        },
        jetIndex = jetIndex,
        shapeIndex = shapeIndex,
    )

    fun part1(input: List<String>): Long {
        val finalState = generateSequence(input.parse()) { it.next() }
            .first { it.count == 2022 }
        return finalState.top.maxOf { it.y }
    }

    fun part2(input: List<String>): Long {
        val stateBySignature = mutableMapOf<StateSignature, State>()

        // look for equivalent states
        var cycleStartOrNull: State? = null
        var cycleEndOrNull: State? = null
        for (state in generateSequence(input.parse()) { it.next() }) {
            val signature = state.toSignature()
            when (val previous = stateBySignature[signature]) {
                null -> stateBySignature[signature] = state
                else -> {
                    cycleStartOrNull = previous
                    cycleEndOrNull = state
                    break
                }
            }
        }

        val cycleStartState = cycleStartOrNull ?: error("shouldn't get here")
        val cycleEndState = cycleEndOrNull ?: error("shouldn't get here")
        val cycleDeltaY = cycleEndState.top.maxOf { it.y } - cycleStartState.top.maxOf { it.y }
        val cycles = (1_000_000_000_000L - cycleStartState.count) / (cycleEndState.count - cycleStartState.count)
        val offset = (1_000_000_000_000L - cycleStartState.count) % (cycleEndState.count - cycleStartState.count)

        val offsetState = generateSequence(cycleStartState) { it.next() }
            .first { it.count == cycleStartState.count + offset.toInt() }
        val offsetDeltaY = offsetState.top.maxOf { it.y } - cycleStartState.top.maxOf { it.y }

        return cycleStartState.top.maxOf { it.y } + (cycleDeltaY * cycles) + offsetDeltaY
    }

    val testInput = readInput("Day17_test")
    check(part1(testInput) == 3068L)
    check(part2(testInput) == 1514285714288L)

    val input = readInput("Day17")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
