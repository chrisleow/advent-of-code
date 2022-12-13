fun main() {

    data class MonkeySpec(
        val id: Int,
        val startingItems: List<Long>,
        val operation: (Long) -> Long,
        val divisor: Int,
        val targetMonkeyIdIfTrue: Int,
        val targetMonkeyIdIfFalse: Int,
    )

    data class State(
        val specs: List<MonkeySpec>,
        val items: List<Pair<Int, Long>>,
        val inspections: Map<Int, Int>,
        val commonDivisorFactor: Int,
    )

    fun List<String>.parse(): State {
        val specs = this
            .split { it.isBlank() }
            .filter { it.isNotEmpty() }
            .mapIndexed { monkeyId, lines ->
                val blankState = MonkeySpec(monkeyId, emptyList(), { it }, 0, 0, 0)
                lines.fold(blankState) { state, line ->
                    val sections = line.split(":").map { it.trim() }
                    val value = sections.last()
                    val words = value.split(" ").map { it.trim() }
                    when (sections[0]) {
                        "Starting items" -> {
                            state.copy(startingItems = value.split(",").map { it.trim().toLong() })
                        }
                        "Operation" -> when {
                            value == "new = old + old" -> state.copy(operation = { it + it })
                            value == "new = old * old" -> state.copy(operation = { it * it })
                            value.startsWith("new = old +") -> {
                                state.copy(operation = { it + words.last().toInt() })
                            }
                            value.startsWith("new = old *") -> {
                                state.copy(operation = { it * words.last().toInt() })
                            }
                            else -> error("Couldn't parse '${sections[1]}'.")
                        }
                        "Test" -> state.copy(divisor = words.last().toInt())
                        "If true" -> state.copy(targetMonkeyIdIfTrue = words.last().toInt())
                        "If false" -> state.copy(targetMonkeyIdIfFalse = words.last().toInt())
                        else -> state
                    }
                }
            }
        return State(
            specs = specs,
            items = specs.flatMap { spec -> spec.startingItems.map { Pair(spec.id, it) } },
            inspections = emptyMap(),
            commonDivisorFactor = specs.map { it.divisor }.reduce { a, b -> a * b }
        )
    }

    fun State.next(reduceWorry: Boolean) = this.specs.fold(this) { state, spec ->
        val existingItems = state.items
            .filter { (id, _) -> id != spec.id }
        val replacementItems = state.items
            .filter { (id, _) -> id == spec.id }
            .map { (_, oldWorry) ->
                val newWorry = if (reduceWorry) {
                    spec.operation(oldWorry) / 3
                } else {
                    spec.operation(oldWorry) % state.commonDivisorFactor
                }
                if (newWorry % spec.divisor == 0L) {
                    Pair(spec.targetMonkeyIdIfTrue, newWorry)
                } else {
                    Pair(spec.targetMonkeyIdIfFalse, newWorry)
                }
            }
        state.copy(
            items = existingItems + replacementItems,
            inspections = state.inspections +
                    (spec.id to (state.inspections[spec.id] ?: 0) + replacementItems.size),
        )
    }

    fun part1(input: List<String>) = generateSequence(input.parse()) { it.next(true) }
        .drop(20)
        .first()
        .let { state ->
            val counts = state.inspections.values.sortedDescending()
            counts[0] * counts[1]
        }

    fun part2(input: List<String>) = generateSequence(input.parse()) { it.next(false) }
        .drop(10000)
        .first()
        .let { state ->
            val counts = state.inspections.values.sortedDescending()
            counts[0].toLong() * counts[1].toLong()
        }

    val testInput = readInput("Day11_test")
    check(part1(testInput) == 10605)
    check(part2(testInput) == 2713310158L)

    val input = readInput("Day11")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}