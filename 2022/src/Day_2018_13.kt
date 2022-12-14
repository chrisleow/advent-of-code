fun main() {

    data class Point(
        val x: Int,
        val y: Int,
    )
    data class CartState(
        val point: Point,
        val previousPoint: Point,
        val direction: Char,
        val nextTurn: Char,
        val isCrashed: Boolean,
    )
    data class State(
        val map: Map<Point, Char>,
        val carts: List<CartState>,
    )

    fun List<String>.parse() = this
        .filter { it.isNotBlank() }
        .flatMapIndexed { y, line -> line.mapIndexed { x, char -> Point(x, y) to char } }
        .fold(State(emptyMap(), emptyList())) { state, (point, char) ->
            when (char) {
                '>', '<' -> state.copy(
                    map = state.map + (point to '-'),
                    carts = state.carts + CartState(point, point, char, 'L', false),
                )
                '^', 'v' -> state.copy(
                    map = state.map + (point to '|'),
                    carts = state.carts + CartState(point, point, char, 'L', false),
                )
                '/', '\\', '-', '|', '+' -> state.copy(
                    map = state.map + (point to char),
                )
                else -> state
            }
        }

    fun State.next(): State {
        val filteredCarts = this.carts
            .filter { !it.isCrashed }
        val newCarts = filteredCarts
            .sortedWith(compareBy({ it.point.x }, { it.point.y }))
            .map { cart ->

                // point the cart in the right direction, without moving yet
                val turnedCart = when (this.map[cart.point]) {
                    '-' -> when (cart.direction) {
                        '>', '<' -> cart
                        else -> error("shouldn't get here.")
                    }
                    '|' -> when (cart.direction) {
                        '^', 'v' -> cart
                        else -> error("shouldn't get here.")
                    }
                    '/' -> when (cart.direction) {
                        '>' -> cart.copy(direction = '^')
                        '<' -> cart.copy(direction = 'v')
                        '^' -> cart.copy(direction = '>')
                        'v' -> cart.copy(direction = '<')
                        else -> error("shouldn't get here.")
                    }
                    '\\' -> when (cart.direction) {
                        '>' -> cart.copy(direction = 'v')
                        '<' -> cart.copy(direction = '^')
                        '^' -> cart.copy(direction = '<')
                        'v' -> cart.copy(direction = '>')
                        else -> error("shouldn't get here.")
                    }
                    '+' -> {
                        when (cart.direction) {
                            '>' -> when (cart.nextTurn) {
                                'L' -> cart.copy(direction = '^', nextTurn = 'S')
                                'S' -> cart.copy(direction = '>', nextTurn = 'R')
                                'R' -> cart.copy(direction = 'v', nextTurn = 'L')
                                else -> error("shouldn't get here.")
                            }
                            '<' -> when (cart.nextTurn) {
                                'L' -> cart.copy(direction = 'v', nextTurn = 'S')
                                'S' -> cart.copy(direction = '<', nextTurn = 'R')
                                'R' -> cart.copy(direction = '^', nextTurn = 'L')
                                else -> error("shouldn't get here.")
                            }
                            '^' -> when (cart.nextTurn) {
                                'L' -> cart.copy(direction = '<', nextTurn = 'S')
                                'S' -> cart.copy(direction = '^', nextTurn = 'R')
                                'R' -> cart.copy(direction = '>', nextTurn = 'L')
                                else -> error("shouldn't get here.")
                            }
                            'v' -> when (cart.nextTurn) {
                                'L' -> cart.copy(direction = '>', nextTurn = 'S')
                                'S' -> cart.copy(direction = 'v', nextTurn = 'R')
                                'R' -> cart.copy(direction = '<', nextTurn = 'L')
                                else -> error("shouldn't get here.")
                            }
                            else -> error("shouldn't get here.")
                        }
                    }
                    else -> error("shouldn't get here.")
                }

                // move forward one point in the correct direction
                turnedCart.copy(
                    point = when (turnedCart.direction) {
                        '>' -> Point(turnedCart.point.x + 1, turnedCart.point.y)
                        '<' -> Point(turnedCart.point.x - 1, turnedCart.point.y)
                        '^' -> Point(turnedCart.point.x, turnedCart.point.y - 1)
                        'v' -> Point(turnedCart.point.x, turnedCart.point.y + 1)
                        else -> error("not a valid cart char ${turnedCart.direction}.")
                    },
                    previousPoint = turnedCart.point,
                )
            }

        return this.copy(
            carts = newCarts.map { newCart ->
                val crossCrash = filteredCarts
                    .any { c -> (newCart.point == c.previousPoint) && (newCart.previousPoint == c.point) }
                val collideCrash = newCarts
                    .any { c -> (newCart.point == c.point) && (newCart != c) }
                newCart.copy(isCrashed = crossCrash || collideCrash)
            }
        )
    }

    fun part1(input: List<String>): String {
        return generateSequence(input.parse()) { it.next() }
            .map { state ->
                state
            }
            .firstNotNullOf { state -> state.carts.firstOrNull { it.isCrashed } }
            .let { cart -> "${cart.point.x},${cart.point.y}" }
    }

    fun part2(input: List<String>): String {
        return generateSequence(input.parse()) { it.next() }
            .dropWhile { state -> state.carts.size > 1 }
            .first()
            .carts
            .map { "${it.point.x},${it.point.y}" }
            .first()
    }

    val testInput = readInput("Day_2018_13_test")
    check(part1(testInput) == "2,0")
    // check(part2(testInput) == "6,4")

    val input = readInput("Day_2018_13")
    println("Part 1: ${part1(input)}")
    // println("Part 2: ${part2(input)}")
}