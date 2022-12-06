import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = File("src", "$name.txt")
    .readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * Split a sequence into sub-lists based on predicate
 */
fun <T> Sequence<T>.split(predicate: (T) -> Boolean): Sequence<List<T>> {
    val source = this
    return sequence {
        var current = mutableListOf<T>()
        source.forEach { item ->
            if (predicate(item)) {
                yield(current)
                current = mutableListOf()
            } else {
                current.add(item)
            }
        }
        if (current.isNotEmpty()) {
            yield(current)
        }
    }
}

/**
 * Split an iterable into sub-lists based on predicate
 */
fun <T> Iterable<T>.split(predicate: (T) -> Boolean): List<List<T>> = this
    .asSequence()
    .split(predicate)
    .toList()
