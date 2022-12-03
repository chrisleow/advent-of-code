import kotlin.math.abs

fun main() {

    data class Vector(val x: Int, val y: Int, val z: Int)
    data class OrientedVectorSet(val displacement: Vector, val vectors: List<Vector>)

    val vectorPattern = "([0-9-]+),([0-9-]+),([0-9-]+)".toRegex()

    fun readInput(input: List<String>): List<List<Vector>> = input
        .split { it.isBlank() }
        .map { lines ->
            lines.mapNotNull { line ->
                vectorPattern.matchEntire(line)?.let { match ->
                    val groupInt = { index: Int -> match.groupValues[index].toInt() }
                    Vector(groupInt(1), groupInt(2), groupInt(3))
                }
            }
        }

    fun getOrientations(vectors: List<Vector>) = sequence {
        val orderedPermutations = sequenceOf<(Vector) -> Vector>(
            { (x, y, z) -> Vector(x, y, z) },
            { (x, y, z) -> Vector(x, z, -y) },
            { (x, y, z) -> Vector(y, x, -z) },
            { (x, y, z) -> Vector(y, z, x) },
            { (x, y, z) -> Vector(z, x, y) },
            { (x, y, z) -> Vector(z, y, -x) },
        )
        orderedPermutations.forEach { transform ->
            yield(vectors.map { v -> transform(v).let { (x, y, z) -> Vector(x, y, z) }})
            yield(vectors.map { v -> transform(v).let { (x, y, z) -> Vector(-x, -y, z) }})
            yield(vectors.map { v -> transform(v).let { (x, y, z) -> Vector(x, -y, -z) }})
            yield(vectors.map { v -> transform(v).let { (x, y, z) -> Vector(-x, y, -z) }})
        }
    }

    fun List<List<Vector>>.resolveScanners(): List<OrientedVectorSet> {
        val unresolvedScannerVectors = this
        val relativeDistancesByScanner = unresolvedScannerVectors.map { vectors ->
            vectors
                .zipAll()
                .map { (v1, v2) -> abs(v1.x - v2.x) + abs(v1.y - v2.y) + abs(v1.z - v2.z) }
                .toSet()
        }

        tailrec fun resolve(resolvedVectorSets: Map<Int, OrientedVectorSet>): List<OrientedVectorSet> {
            if (resolvedVectorSets.size == size) {
                return resolvedVectorSets.entries
                    .sortedBy { it.key }
                    .map { it.value }
            }

            val newResolvedOrientedVectorSetEntry = unresolvedScannerVectors.indices
                .asSequence()
                .filter { it !in resolvedVectorSets }
                .flatMap { unresolvedIndex ->
                    val relativeDistances = relativeDistancesByScanner[unresolvedIndex]
                    resolvedVectorSets.keys.mapNotNull { resolvedIndex ->
                        val resolvedRelativeDistances = relativeDistancesByScanner[resolvedIndex]
                        if ((relativeDistances intersect resolvedRelativeDistances).size >= 66) {
                            Pair(resolvedIndex, unresolvedIndex)
                        } else {
                            null
                        }
                    }
                }
                .flatMap { (resolvedIndex, unresolvedIndex) ->
                    val resolvedVectors = resolvedVectorSets[resolvedIndex]?.vectors
                        ?: error("Bad Index")

                    getOrientations(this[unresolvedIndex]).flatMap { orientedVectors ->
                        resolvedVectors
                            .flatMap { resolved ->
                                orientedVectors.map { vector ->
                                    Vector(
                                        x = resolved.x - vector.x,
                                        y = resolved.y - vector.y,
                                        z = resolved.z - vector.z,
                                    )
                                }
                            }
                            .groupingBy { it }
                            .eachCount()
                            .asSequence()
                            .filter { (_, count) -> count >= 12 }
                            .map { (displacement, _) ->
                                val newOrientedVectorSet = OrientedVectorSet(
                                    displacement = displacement,
                                    vectors = orientedVectors.map {
                                        Vector(
                                            x = it.x + displacement.x,
                                            y = it.y + displacement.y,
                                            z = it.z + displacement.z,
                                        )
                                    },
                                )

                                // new resolved vector set
                                Pair(unresolvedIndex, newOrientedVectorSet)
                            }
                    }
                }
                .firstOrNull()
                ?: error("Cannot find anything else to resolve against, quitting here.")

            return resolve(resolvedVectorSets + newResolvedOrientedVectorSetEntry)
        }

        val initialVectorSet = OrientedVectorSet(Vector(0, 0, 0), this[0])
        return resolve(mapOf(0 to initialVectorSet))
    }

    fun part1(input: List<String>) = readInput(input)
        .resolveScanners()
        .flatMap { it.vectors }
        .distinct()
        .count()

    fun part2(input: List<String>) = readInput(input)
        .resolveScanners()
        .map { it.displacement }
        .zipAll()
        .maxOf { (v1, v2) -> abs(v1.x - v2.x) + abs(v1.y - v2.y) + abs(v1.z - v2.z) }

    val testInput = readInput("Day19_test")
    check(part1(testInput) == 79)
    check(part2(testInput) == 3621)

    val input = readInput("Day19")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
