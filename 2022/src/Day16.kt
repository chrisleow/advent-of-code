fun main() {

    data class Valve(val room: String, val flowRate: Int, val linkedRooms: List<String>)
    data class AdjacencyMap(val distances: Map<Pair<String, String>, Int>, val flowRates: Map<String, Int>)
    data class Path(val remaining: Int, val pressure: Int, val path: List<String>)

    fun List<String>.parse(): List<Valve> {
        val regex = ".*([A-Z]{2}).*=(\\d+).*valves?\\s+([A-Z, ]+)".toRegex()
        return this
            .mapNotNull { line -> regex.matchEntire(line)?.groupValues }
            .map { gv -> Valve(gv[1], gv[2].toInt(), gv[3].split(",").map { it.trim() }) }
    }

    fun List<Valve>.toAdjacencyMap(): AdjacencyMap {
        val valves = this

        // build full adjacency map (wanna stay fully functional)
        val distances = run {
            val initialDistances = valves.associate { valve ->
                valve.room to buildMap {
                    put(valve.room, 0)
                    valve.linkedRooms.forEach { put(it, 1) }
                }
            }

            val expandingDistanceMaps = generateSequence(initialDistances) { distances ->
                val expandedDistances = distances
                    .flatMap { (start, subDistances) ->
                        subDistances.flatMap { (intermediate, distance) ->
                            distances[intermediate]
                                ?.map { (end, extraDistance) -> start to (end to (distance + extraDistance)) }
                                ?: emptyList()
                        }
                    }
                    .groupBy({ (start, _) -> start }) { (_, subDistances) -> subDistances }
                    .mapValues { (_, endDistances) ->
                        endDistances
                            .groupBy({ (end, _) -> end }) { (_, distance) -> distance }
                            .mapValues { (_, distances) -> distances.min() }
                    }

                // terminate once we've fully expanded the map
                if (distances == expandedDistances) null else expandedDistances
            }

            expandingDistanceMaps
                .last()
                .flatMap { (start, ds) -> ds.map { (end, distance) -> (start to end) to distance } }
                .toMap()
        }

        // restrict to points of interest only
        val pointsOfInterest = (this.filter { it.flowRate > 0 }.map { it.room } + "AA")
        return AdjacencyMap(
            distances = distances
                .filter { it.key.first in pointsOfInterest && it.key.second in pointsOfInterest },
            flowRates = this
                .filter { it.room in pointsOfInterest }
                .associate { it.room to it.flowRate },
        )
    }

    fun getBestPressureByOpened(adjacencyMap: AdjacencyMap, minutes: Int): Map<Set<String>, Int> {
        val destinationRooms = adjacencyMap.flowRates
            .filter { (_, rate) -> rate > 0 }
            .keys
            .toList()

        // keep track of best paths by unique signature, Pair(Room, OpenedValves)
        val initialMap = mapOf(Pair("AA", emptySet<String>()) to Path(minutes, 0, listOf("AA")))
        val bestPathSignatureMaps = generateSequence(initialMap) { bestPaths ->
            destinationRooms
                .asSequence()
                .flatMap { room ->
                    bestPaths.mapNotNull { (_, path) ->
                        if (room in path.path) {
                            return@mapNotNull null
                        }

                        val previousRoom = path.path.last()
                        val distance = (adjacencyMap.distances[previousRoom to room] ?: 1_000_000_000)
                        val newRemaining = path.remaining - distance - 1
                        if (newRemaining <= 0) {
                            return@mapNotNull null
                        }

                        val addedPressure = newRemaining * (adjacencyMap.flowRates[room] ?: 0)
                        Path(newRemaining, path.pressure + addedPressure, path.path + room)
                    }
                }
                .plus(bestPaths.values)
                .groupingBy { Pair(it.path.last(), it.path.toSet()) }
                .aggregate { _, bestPathSoFar: Path?, path, _ ->
                    listOfNotNull(bestPathSoFar, path).maxBy { it.pressure }
                }
        }

        // every possible path covered when the set doesn't increase in size
        val bestPathBySignature = bestPathSignatureMaps
            .windowed(2)
            .takeWhile { maps -> maps[1].size > maps[0].size }
            .map { maps -> maps[1] }
            .last()
        return bestPathBySignature.values
            .groupingBy { it.path.toSet() - "AA" }
            .aggregate { _, pressure: Int?, path, _ -> maxOf(pressure ?: 0, path.pressure) }
    }

    fun part1(input: List<String>): Int {
        return getBestPressureByOpened(input.parse().toAdjacencyMap(), 30).values.max()
    }

    fun part2(input: List<String>): Int {
        val bestPressureByOpened = getBestPressureByOpened(input.parse().toAdjacencyMap(), 26)
        val bestAntiPressureByOpened = bestPressureByOpened
            .mapValues { (opened, _) ->
                bestPressureByOpened.entries
                    .filter { (antiOpened, _) -> (opened intersect antiOpened).isEmpty() }
                    .maxOf { (_, pressure) -> pressure }
            }
        return bestPressureByOpened
            .maxOf { (opened, pressure) -> pressure + (bestAntiPressureByOpened[opened] ?: 0) }
    }

    val testInput = readInput("Day16_test")
    check(part1(testInput) == 1651)
    check(part2(testInput) == 1707)

    val input = readInput("Day16")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
