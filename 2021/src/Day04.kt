fun main() {

    data class Point(val x: Int, val y: Int)
    data class BingoRecord(val move: Int, val number: Int)
    data class Grid(val numbers: Map<Point, Int>, val marks: Set<Point>, val bingo: BingoRecord?)
    data class State(val move: Int, val remainingNumbers: List<Int>, val grids: List<Grid>)

    fun State.print() {
        println("Remaining Numbers: ${this.remainingNumbers}")
        println()
        this.grids.forEach { grid ->
            (0 until 5).forEach { y ->
                (0 until 5).forEach { x ->
                    val point = Point(x, y)
                    val number = grid.numbers[point]
                    val mark = if (point in grid.marks) "*" else " "
                    print("$number$mark".padStart(4))
                }
                println()
            }
            if (grid.bingo != null) {
                println(" !!! BINGO (Move: ${grid.bingo.move}, Number: ${grid.bingo.number}) !!!")
            }
            println()
        }
    }

    fun <T> Iterable<T>.split(isDelimiter: (T) -> Boolean): List<List<T>> = this
        .fold(listOf(emptyList<T>())) { lists, element ->
            when (isDelimiter(element)) {
                true -> lists + listOf(emptyList())
                false -> lists.dropLast(1) + listOf(lists.last() + element)
            }
        }
        .filter { it.isNotEmpty() }

    fun parseInput(input: List<String>): State {
        val groups = input.split { it.isBlank() }
        val numbers = groups
            .first()
            .joinToString(",")
            .split(",")
            .filter { it.isNotBlank() }
            .map { it.toInt() }
        val grids = groups
            .drop(1)
            .map { lines ->
                val gridNumbers = buildMap<Point, Int> {
                    lines.forEachIndexed { y, line ->
                        line.split(" ")
                            .filter { it.isNotBlank() }
                            .forEachIndexed { x, number -> put(Point(x, y), number.toInt()) }
                    }
                }
                Grid(numbers = gridNumbers, marks = emptySet(), bingo = null)
            }
        return State(move = 0, remainingNumbers = numbers, grids = grids)
    }

    val winningPositions = run {
        val positions = listOf(
            (0 until 5).map { y -> (0 until 5).map { x -> Point(x, y) }.toSet() },
            (0 until 5).map { x -> (0 until 5).map { y -> Point(x, y) }.toSet() },
        )
        positions.flatten()
    }

    fun State.next(): State? {
        val currentMove = this.move + 1
        val currentNumber = this.remainingNumbers.firstOrNull() ?: return null

        val newGrids = this.grids.map { grid ->
            val matchingPositions = grid.numbers
                .filter { (_, number) -> number == currentNumber }
                .map { (pos, _) -> pos }

            val newMarks = grid.marks + matchingPositions
            val newBingo = grid.bingo ?: run {
                if (winningPositions.any { newMarks.containsAll(it) }) {
                    BingoRecord(currentMove, currentNumber)
                } else {
                    null
                }
            }

            grid.copy(marks = newMarks, bingo = newBingo)
        }

        return State(move = currentMove, remainingNumbers = this.remainingNumbers.drop(1), grids = newGrids)
    }

    fun Grid.getScore(): Int {
        return when (this.bingo) {
            null -> error("No bingo, no score :(")
            else -> {
                val unmarkedSum = this.numbers
                    .filter { (pos, _) -> pos !in this.marks }
                    .map { (_, number) -> number }
                    .sum()
                unmarkedSum * this.bingo.number
            }
        }
    }

    fun part1(input: List<String>): Int {
        val initialState = parseInput(input)
        val states = generateSequence(initialState) { it.next() }
        return states
            .firstNotNullOf { state -> state.grids.firstOrNull { it.bingo != null } }
            .getScore()
    }

    fun part2(input: List<String>): Int {
        val initialState = parseInput(input)
        val states = generateSequence(initialState) { it.next() }
        return states
            .first { state -> state.grids.all { it.bingo != null } }
            .grids
            .maxByOrNull { it.bingo?.move ?: 0 }
            ?.getScore()
            ?: error("should have a losing grid")
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput) == 4512)
    check(part2(testInput) == 1924)

    val input = readInput("Day04")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
