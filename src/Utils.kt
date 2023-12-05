import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)

/**
 * Mimic Reactor `Publisher<T>.expand(Function<T, Publisher<T>)`
 */
fun <T> Sequence<T>.expand(action: (T) -> Sequence<T>) : Sequence<T> {
    return flatMap {
        sequenceOf(it) + action(it).expand(action)
    }
}
