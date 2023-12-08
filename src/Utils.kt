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

fun <T> List<Sequence<T>>.zipAll(failIfNotSameSize : Boolean = true) : Sequence<List<T>> {
    val iters = map { it.iterator() }
    return sequence {
        var ended = 0
        do {
            val nextBag = ArrayList<T>(iters.size)
            for (iter in iters) {
                if (iter.hasNext()) nextBag.add(iter.next())
                else ended += 1
            }
            if (ended == 0) yield(nextBag)
        } while (ended == 0)

        if (failIfNotSameSize && ended < iters.size) throw IllegalStateException("$ended sequences finished early out of all ${iters.size}")
    }
}
