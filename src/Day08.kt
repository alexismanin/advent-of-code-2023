package day08

import println
import readInput
import zipAll
import java.util.*
import kotlin.collections.HashMap
import kotlin.time.measureTime


fun main() {
    val input = readInput("Day08")
    val map = parse(input)

    // part1(map).println()
    // NOTE: confirmed on submission that 78 billion is too low. I have to find another way. Brut-force is not gonna make it !
    optimizedPart2(map).println()
}

fun part1(map: DirectionMap) = map.computePath().count()
fun part2(map: DirectionMap) : Int {
    val startForks = map.forks.values.filter { it.loc.endsWith('A') }
    return startForks.asSequence()
        .map { map.computePath(from=it.loc, to=null) }
        .toList().zipAll()
        .takeWhile {
            forks ->
            !forks.all {
                (fork, _) ->
                fork.loc.endsWith('Z')
            }
        }
        .count()
}


fun optimizedPart2(map: DirectionMap) : Long {
    val indexedForks = index(map)
    val nextForks = indexedForks.filter { it.loc.endsWith('A') }.toTypedArray()
    val forkCount = nextForks.size
    val nbInstructions = map.instructions.size
    var count = 0L
    var endWithZ = 0
    val directions = map.instructions.map { it == Direction.left }
    while (endWithZ < forkCount) {
        endWithZ = 0
        val turnLeft = directions[(count++ % nbInstructions).toInt()]
        if (count % 1_000_000_000 == 0L) println("COUNT: $count")
        for (i in 0..<forkCount) {
            val nextFork = if (turnLeft) nextForks[i].left else nextForks[i].right
            nextForks[i] = indexedForks[nextFork]
            if (nextForks[i].endsWithZ) endWithZ++
        }
    }
    return count
}

fun parse(input: List<String>): DirectionMap {
    val iter = input.iterator()
    val instructions = iter.next().asSequence().map(Char::toDirection).toList()
    val forks = mutableMapOf<String, Fork>()
    val pattern = Regex("(?<loc>[\\w]{3})\\s*=\\s*\\((?<left>[\\w]{3})\\s*,\\s*(?<right>[\\w]{3})\\)")
    while (iter.hasNext()) {
        val line = iter.next()
        if (line.isBlank()) continue
        val match = pattern.matchEntire(line) ?: throw IllegalArgumentException("Line does not match pattern: $line")
        val fork = Fork(match.groups["loc"]!!.value, match.groups["left"]!!.value, match.groups["right"]!!.value)
        forks[fork.loc] = fork
    }

    return DirectionMap(instructions, forks)
}

fun Char.toDirection() = when (this) {
    'L' -> Direction.left
    'R' -> Direction.right
    else -> throw IllegalArgumentException("Bad direction: $this")
}

enum class Direction {
    left, right
}

data class Fork(val loc: String, val left: String, val right: String)

data class DirectionMap(val instructions: List<Direction>, val forks: Map<String, Fork>) {

    /**
     * @param to The destination fork. WARNING: If it is null, an infinite sequence is generated.
     */
    fun computePath(from: String = "AAA", to: String? = "ZZZ", startOffset : Int = 0) : Sequence<Pair<Fork, Direction>> {
        var nextFork = requireNotNull(forks[from]) { "Start path does not exist: $from" }
        if (from == to) return emptySequence()
        return sequence {
            var offset = startOffset
            do {
                yield(nextFork to offset)
                val direction = if (instructions[offset] == Direction.left) nextFork.left else nextFork.right
                nextFork = forks[direction] ?: throw IllegalStateException("Unfinished path !")
                if (++offset >= instructions.size) offset = 0
            } while(nextFork.loc != to)
        }
            .map { (fork, offset) -> fork to instructions[offset] }
    }
}

data class IndexedFork(val loc: String, val left: Int, val right: Int, val endsWithZ: Boolean)

fun index(map: DirectionMap) : List<IndexedFork> {
    val sortedKeys = map.forks.keys.sorted()
    val forkIndices = sortedKeys.foldIndexed(HashMap<String, Int>(sortedKeys.size)) { idx, acc, loc -> acc.put(loc, idx) ; acc }
    val indexedForks = sortedKeys.map {
        loc ->
        val fork = map.forks[loc]!!
        IndexedFork(loc, forkIndices[fork.left]!!, forkIndices[fork.right]!!, loc.endsWith('Z'))
    }
    return indexedForks
}
