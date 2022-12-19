import kotlin.math.ceil

fun main() {

    data class Blueprint(val id: Int, val costs: Map<String, Map<String, Int>>)
    data class Path(val resources: Map<String, Int>, val robots: Map<String, Int>, val remaining: Int)
    data class PathSignature(val robots: Map<String, Int>, val remaining: Int)

    fun List<String>.parse(): List<Blueprint> {
        val costsRegex = "Each\\s+(\\w+)\\s+robot\\s+costs\\s+(.*)".toRegex()
        val costRegex = "(\\d+)\\s+(\\w+)".toRegex()
        return this
            .filter { it.isNotBlank() }
            .map { line ->
                val sections1 = line.split(":")
                val sections2 = sections1[1].split(".")
                Blueprint(
                    id = sections1[0].split(" ").last().toInt(),
                    costs = sections2
                        .mapNotNull { costsRegex.matchEntire(it.trim())?.groupValues }
                        .associate { groupValues ->
                            val costs = costRegex
                                .findAll(groupValues[2])
                                .associate { m -> m.groupValues[2] to m.groupValues[1].toInt() }
                            (groupValues[1] to costs)
                        }
                )
            }
    }

    fun Path.toSignature() = PathSignature(robots = robots, remaining = remaining)

    fun Path.getNext(blueprint: Blueprint) = sequence {

        // times we have to wait, if we want a new robot of a given type
        val newRobotWaitingTimes = blueprint.costs
            .mapNotNull { (resource, costs) ->
                val times = costs.map { (requiredResource, cost) ->
                    val count = resources[requiredResource] ?: 0
                    val rate = robots[requiredResource] ?: 0
                    when {
                        count >= cost -> 1
                        rate == 0 -> null
                        else -> ceil((cost - count).toFloat() / rate).toInt() + 1
                    }
                }

                if (times.any { it == null }) {
                    null
                } else {
                    resource to times.maxOf { it ?: 0 }
                }
            }
            .toMap()

        // the "do nothing" case
        yield(
            copy(
                remaining = 0,
                resources = (resources.keys + robots.keys)
                    .associateWith { r -> (resources[r] ?: 0) + ((robots[r] ?: 0) * remaining) },
            )
        )

        // try to build each robot in turn
        for ((robotResource, costs) in blueprint.costs) {
            val elapsed = newRobotWaitingTimes[robotResource] ?: Int.MAX_VALUE
            if (elapsed > remaining) {
                continue
            }

            yield(
                copy(
                    resources = run {
                        (resources.keys + robots.keys).associateWith { resource ->
                            (resources[resource] ?: 0) +
                                    ((robots[resource] ?: 0) * elapsed) -
                                    (costs[resource] ?: 0)
                        }
                    },
                    robots = robots + (robotResource to (robots[robotResource] ?: 0) + 1),
                    remaining = remaining - elapsed,
                )
            )
        }
    }

    fun getMaxGeodes(blueprint: Blueprint, minutes: Int): Int {

        fun Path.getFitness(): Int {
            val numbers = listOf(
                resources["geode"] ?: 0,
                resources["obsidian"] ?: 0,
                resources["clay"] ?: 0,
                resources["ore"] ?: 0,
            )
            return numbers.fold(0) { acc, n -> (acc * 100) + n }
        }

        // tail recursive breadth-first search
        tailrec fun fill(best: Map<PathSignature, List<Path>>): Map<PathSignature, List<Path>> {
            val newBest = best
                .flatMap { (_, paths) -> paths.flatMap { it.getNext(blueprint) } }
                .groupBy { path -> path.toSignature() }
                .mapValues { (_, paths) ->
                    // note: this seems to work at the top 3 paths even, but is absolutely horrible
                    // algorithmically ...
                    paths.sortedByDescending { it.getFitness()}.take(5)
                }
            return when (newBest) {
                best -> best
                else -> fill(newBest)
            }
        }

        // examine all paths
        val initialPath = Path(resources = emptyMap(), robots = mapOf("ore" to 1), remaining = minutes)
        val bestPaths = fill(mapOf(initialPath.toSignature() to listOf(initialPath)))
        return bestPaths.values.flatten().maxOf { it.resources["geode"] ?: 0 }
    }

    fun part1(input: List<String>): Int {
        val blueprints = input.parse()
        return blueprints.sumOf { bp -> bp.id * getMaxGeodes(bp, 24) }
    }

    fun part2(input: List<String>): Int {
        val blueprints = input.parse().take(3)
        return blueprints
            .map { bp -> getMaxGeodes(bp, 32) }
            .reduce { a, b -> a * b }
    }

    val testInput = readInput("Day19_test")
    check(part1(testInput) == 33)
    check(part2(testInput) == 56 * 62)

    val input = readInput("Day19")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
