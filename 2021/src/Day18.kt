sealed class Expr {
    data class Pair(val left: Expr, val right: Expr) : Expr()
    data class Literal(val value: Int) : Expr()
}

fun main() {

    fun parseLine(line: String): Expr {
        fun parse(index: Int): Pair<Int, Expr> = when (line[index]) {
            '[' -> {
                val (index1, left) = parse(index + 1)
                assert(line[index1 + 1] == ',')
                val (index2, right) = parse(index1 + 1)
                assert(line[index2] == ']')
                (index2 + 1) to Expr.Pair(left, right)
            }
            else -> {
                val digits = line.drop(index).takeWhile { it.isDigit() }
                (index + digits.length) to Expr.Literal(digits.toInt())
            }
        }
        return parse(0).second
    }

    fun parseInput(input: List<String>): List<Expr> {
        return input.map { line -> parseLine(line) }
    }

    fun Expr.toDisplayString(): String = when (this) {
        is Expr.Literal -> value.toString()
        is Expr.Pair -> "[${left.toDisplayString()},${right.toDisplayString()}]"
    }

    fun Expr.explode(): Expr {

        fun Expr.addLeft(valueToAdd: Int): Expr = when (this) {
            is Expr.Literal -> Expr.Literal(value + valueToAdd)
            is Expr.Pair -> copy(left = left.addLeft(valueToAdd))
        }

        fun Expr.addRight(valueToAdd: Int): Expr = when (this) {
            is Expr.Literal -> Expr.Literal(value + valueToAdd)
            is Expr.Pair -> copy(right = right.addRight(valueToAdd))
        }

        // replace inner level and propagate out expected left and right additions recursively
        //
        // in order to limit the entire operation to one operation only, we use nulls to indicate "no modification"
        // so there is only one "explosion" recursively at a time.
        fun Expr.getExploded(level: Int): Triple<Int, Expr, Int>? {
            when (this) {
                is Expr.Literal -> {
                    return null
                }
                is Expr.Pair -> {
                    if (level == 4) {
                        val leftVal = (left as? Expr.Literal)?.value ?: error("expected literal at level 4")
                        val rightVal = (right as? Expr.Literal)?.value ?: error("expected literal at level 4")
                        return Triple(leftVal, Expr.Literal(0), rightVal)
                    }

                    // if anything below on the left is exploded, absorb the extra value, propagate left up
                    val leftTriple = left.getExploded(level + 1)
                    if (leftTriple != null) {
                        val (leftVal, newLeftExpr, rightVal) = leftTriple
                        return Triple(leftVal, Expr.Pair(newLeftExpr, right.addLeft(rightVal)), 0)
                    }

                    // if anything below on the right is exploded, absorb the extra value, propagate right up
                    val rightTriple = right.getExploded(level + 1)
                    if (rightTriple != null) {
                        val (leftVal, newRightExpr, rightVal) = rightTriple
                        return Triple(0, Expr.Pair(left.addRight(leftVal), newRightExpr), rightVal)
                    }

                    // nothing to explode
                    return null
                }
            }
        }

        // ignore left and right values coming up
        return this.getExploded(0)?.second ?: this
    }

    val explodeTests = mapOf(
        "[[[[[9,8],1],2],3],4]" to "[[[[0,9],2],3],4]",
        "[7,[6,[5,[4,[3,2]]]]]" to "[7,[6,[5,[7,0]]]]",
        "[[6,[5,[4,[3,2]]]],1]" to "[[6,[5,[7,0]]],3]",
        "[[3,[2,[1,[7,3]]]],[6,[5,[4,[3,2]]]]]" to "[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]",
        "[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]" to "[[3,[2,[8,0]]],[9,[5,[7,0]]]]",
        "[[[[0,7],4],[7,[[8,4],9]]],[1,1]]" to "[[[[0,7],4],[15,[0,13]]],[1,1]]",
    )
    explodeTests.forEach { (input, expected) ->
        val actual = parseLine(input).explode()
        if (parseLine(input).explode() != parseLine(expected)) {
            println("Explode failed! Expected ${expected}, got ${actual.toDisplayString()}")
        }
    }

    // short-cut splitting the right of a pair whenever the left is already split
    fun Expr.split(): Expr {
        return when (this) {
            is Expr.Pair -> when (val leftSplit = left.split()) {
                left -> Expr.Pair(left, right.split())
                else -> Expr.Pair(leftSplit, right)
            }
            is Expr.Literal -> when (value) {
                in (0 .. 9) -> this
                else -> Expr.Pair(Expr.Literal(value / 2), Expr.Literal((value + 1) / 2))
            }
        }
    }

    fun Expr.magnitude(): Int = when (this) {
        is Expr.Literal -> value
        is Expr.Pair -> (3 * left.magnitude()) + (2 * right.magnitude())
    }

    tailrec fun Expr.reduce(): Expr {
        return when (val exploded = this.explode()) {
            this -> when (val split = this.split()) {
                this -> this
                else -> split.reduce()
            }
            else -> exploded.reduce()
        }
    }

    fun add(left: Expr, right: Expr): Expr {
        return Expr.Pair(left, right).reduce()
    }

    fun part1(input: List<String>): Int {
        return parseInput(input).reduce { acc, expr -> add(acc, expr) }.magnitude()
    }

    fun part2(input: List<String>): Int {
        val snailNumbers = parseInput(input)
        return snailNumbers
            .flatMap { n1 -> snailNumbers.filter { it != n1 }.map { n2 -> add(n1, n2) } }
            .maxOf { it.magnitude() }
    }

    // test
    val testInput = readInput("Day18_test")
    check(part1(testInput) == 4140)
    check(part2(testInput) == 3993)

    val input = readInput("Day18")
    println("Part 1: ${part1(input)}")
    println("Part 2: ${part2(input)}")
}
