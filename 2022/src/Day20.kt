fun main() {

    class Node(var number: Long) {
        var prev: Node = this
        var next: Node = this
    }

    data class CircularLinkedList(
        val nodes: List<Node>,
        val size: Int,
    )

    val decryptionKey = 811589153L

    fun List<String>.parse(): CircularLinkedList {
        val nodes = this
            .filter { it.isNotBlank() }
            .map { Node(it.toLong()) }
        nodes.forEachIndexed { index, node ->
            node.prev = nodes[(index + nodes.size - 1) % nodes.size]
            node.next = nodes[(index + 1) % nodes.size]
        }
        return CircularLinkedList(nodes, nodes.size)
    }

    tailrec fun CircularLinkedList.navigate(node: Node, displacement: Long): Node {
        return when (displacement) {
            in Long.MIN_VALUE .. -1L -> navigate(node.prev, displacement + 1)
            in 1L .. Long.MAX_VALUE -> navigate(node.next, displacement - 1)
            else -> node
        }
    }

    fun CircularLinkedList.mix() {
        for (node in nodes) {

            // cut out existing node
            node.prev.next = node.next
            node.next.prev = node.prev

            // find new insert site
            val prev = navigate(node.prev, node.number % (size - 1))
            val next = navigate(node.next, node.number % (size - 1))
            prev.next = node
            node.prev = prev
            node.next = next
            next.prev = node
        }
    }

    fun part1(input: List<String>): Long {
        val linkedList = input.parse()
        linkedList.mix()

        val zeroNode = linkedList.nodes.first { it.number == 0L }
        return listOf(1000L, 2000L, 3000L).sumOf { wrappedIndex ->
            linkedList.navigate(zeroNode, wrappedIndex % linkedList.size).number
        }
    }

    fun part2(input: List<String>): Long {
        val linkedList = input.parse()
        linkedList.nodes.forEach { it.number = it.number * decryptionKey }
        repeat(10) { linkedList.mix() }

        val zeroNode = linkedList.nodes.first { it.number == 0L }
        return listOf(1000L, 2000L, 3000L).sumOf { wrappedIndex ->
            linkedList.navigate(zeroNode, wrappedIndex % linkedList.size).number
        }
    }

    val testInput = readInput("Day20_test")
    check(part1(testInput) == 3L)
    check(part2(testInput) == 1623178306L)

    val input = readInput("Day20")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
