import kotlin.math.abs

fun main() {

    data class Vector(val x: Int, val y: Int, val z: Int) {
        operator fun plus(other: Vector) = Vector(x + other.x, y + other.y, z + other.z)
        operator fun minus(other: Vector) = Vector(x - other.x, y - other.y, z - other.z)
        val norm = abs(x) + abs(y) + abs(z)
    }

    data class OrientedScan(
        val id: Int,
        val beacons: Set<Vector>,
        val mutualDistances: Set<Int>,
        val position: Vector,
    )

    fun <T> Iterable<T>.split(delimiter: (T) -> Boolean): List<List<T>> = this
        .fold(listOf(emptyList<T>())) { acc, item ->
            when (delimiter(item)) {
                true -> acc + listOf(emptyList())
                false -> acc.dropLast(1) + listOf(acc.last() + item)
            }
        }
        .filter { it.isNotEmpty() }

    fun parseInput(input: List<String>): List<OrientedScan> {
        val regex = "(-?\\d+),(-?\\d+),(-?\\d+)".toRegex()
        return input
            .split { it.isBlank() }
            .mapIndexed { index, lines ->
                val beacons = lines
                    .mapNotNull { regex.matchEntire(it)?.groupValues }
                    .map { gv -> Vector(gv[1].toInt(), gv[2].toInt(), gv[3].toInt()) }
                OrientedScan(
                    id = index,
                    beacons = beacons.toSet(),
                    position = Vector(0, 0, 0),
                    mutualDistances = beacons
                        .flatMap { b1 -> beacons.map { b2 -> (b1 - b2).norm } }
                        .filter { it > 0 }
                        .toSet(),
                )
            }
    }

    // careful to avoid flips (can't be arsed to do it properly with matrices)
    val orientationTransformers = run {
        val permutations = listOf<(Vector) -> Vector>(
            { (x, y, z) -> Vector(x, y, z) },
            { (x, y, z) -> Vector(x, z, -y) },
            { (x, y, z) -> Vector(y, x, -z) },
            { (x, y, z) -> Vector(y, z, x) },
            { (x, y, z) -> Vector(z, x, y) },
            { (x, y, z) -> Vector(z, y, -x) },
        )
        permutations.flatMap { transform ->
            listOf<(Vector) -> Vector>(
                { v -> transform(v).let { (x, y, z) -> Vector(x, y, z) } },
                { v -> transform(v).let { (x, y, z) -> Vector(-x, -y, z) } },
                { v -> transform(v).let { (x, y, z) -> Vector(x, -y, -z) } },
                { v -> transform(v).let { (x, y, z) -> Vector(-x, y, -z) } },
            )
        }
    }

    fun OrientedScan.transform(transform: (Vector) -> Vector) = copy(
        beacons = beacons.map { transform(it) }.toSet(),
        position = transform(position),
    )

    fun OrientedScan.allOrientations() = orientationTransformers.asSequence().map { this.transform(it) }
    operator fun OrientedScan.plus(other: Vector) = this.transform { it + other }

    fun List<OrientedScan>.orientAll(): List<OrientedScan> {
        tailrec fun orientStep(scans: List<OrientedScan>, knownScans: List<OrientedScan>): List<OrientedScan> {
            if (scans.isEmpty()) return knownScans

            // first-pass filter, we know that if at least 66 == 12 * (12 - 1) / 2 of the norms don't match in
            // length, we shouldn't investigate further...  much more efficient than examining all orientations
            // up-front!
            val matchingScanPairsFirstPass = knownScans
                .asSequence()
                .flatMap { knownScan -> scans.map { Pair(knownScan, it) } }
                .filter { (knownScan, scan) -> (knownScan.mutualDistances intersect scan.mutualDistances).size >= 66 }

            // now have to work harder, attempt an alignment between all points at all orientations until beacon
            // count condition is met
            val newKnownScan = matchingScanPairsFirstPass
                .flatMap { (knownScan, scan) -> scan.allOrientations().map { Pair(knownScan,it) } }
                .firstNotNullOf { (knownScan, orientedScan) ->
                    knownScan.beacons
                        .asSequence()
                        .flatMap { knownBeacon -> orientedScan.beacons.map { beacon -> knownBeacon - beacon } }
                        .distinct()
                        .map { position -> orientedScan + position }
                        .firstOrNull { scan -> (scan.beacons intersect knownScan.beacons).size >= 12 }
                }

            // move scan to "known", so we may try again
            return orientStep(scans.filter { it.id != newKnownScan.id }, knownScans + newKnownScan)
        }

        // start search assuming scanner 0 is the correct orientation and at absolute 0 position
        return orientStep(this.drop(1), listOf(this.first()))
    }

    fun part1(input: List<String>): Int {
        val orientedScans = parseInput(input).orientAll()
        return orientedScans.flatMap { it.beacons }.toSet().size
    }

    fun part2(input: List<String>): Int {
        val orientedScans = parseInput(input).orientAll()
        return orientedScans
            .flatMap { s1 -> orientedScans.map { s2 -> (s1.position - s2.position).norm } }
            .maxOrNull()
            ?: error("no scans to orient")
    }

    // test
    val testInput = readInput("Day19_test")
    check(part1(testInput) == 79)
    check(part2(testInput) == 3621)

    val input = readInput("Day19")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
