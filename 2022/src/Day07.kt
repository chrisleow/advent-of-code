fun main() {

    data class NestedFile(val path: List<String>, val name: String, val size: Int?)

    fun List<String>.parse() = sequence {
        val directoryPath = mutableListOf<String>()
        this@parse.forEach { command ->
            when {
                command.startsWith("$") -> {
                    if (command.startsWith("$ cd")) {
                        when (val directory = command.substring(4).trim()) {
                            "/" -> directoryPath.clear()
                            ".." -> directoryPath.removeLast()
                            else -> directoryPath.add(directory)
                        }
                    }
                }
                command.startsWith("dir") -> {
                    val name = command.substring(4).trim()
                    yield(NestedFile(directoryPath.toList(), name, null))
                }
                else -> {
                    val parts = command.split(" ")
                    yield(NestedFile(directoryPath.toList(), parts[1], parts[0].toInt()))
                }
            }
        }
    }

    fun part1(input: List<String>) = input
        .parse()
        .flatMap { nf -> (0 .. nf.path.size).map { size -> nf.copy(path = nf.path.take(size)) } }
        .groupingBy { nf -> nf.path }
        .aggregate { _, total: Int?, nf, _ -> (total ?: 0) + (nf.size ?: 0) }
        .map { it.value }
        .filter { it <= 100000 }
        .sum()

    fun part2(input: List<String>): Int {
        val sizes = input
            .parse()
            .flatMap { nf -> (0..nf.path.size).map { size -> nf.copy(path = nf.path.take(size)) } }
            .groupingBy { nf -> nf.path }
            .aggregate { _, size: Int?, nf, _ -> (size ?: 0) + (nf.size ?: 0) }

        val totalSize = sizes[emptyList()] ?: error("shouldn't get here.")
        val spaceToFree = 30000000 - (70000000 - totalSize)
        return sizes.values
            .filter { it >= spaceToFree }
            .min()
    }

    val testInput = readInput("Day07_test")
    check(part1(testInput) == 95437)
    check(part2(testInput) == 24933642)

    val input = readInput("Day07")
    println("Part 1 ${part1(input)}")
    println("Part 2 ${part2(input)}")
}