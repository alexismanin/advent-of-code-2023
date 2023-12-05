import kotlin.random.Random

fun main() {
    val input = readInput("Day05")
    val almanac = parse(input)
    // debugMapping(almanac)

    almanac.seeds.windowed(2, 2).sortedBy { it[0] }
        .zipWithNext()
        .any { (p1, p2) -> p1[0] + p1[1] > p2[0] }
        .let { if (it) println("OVERLAP !") }
    part1(almanac).println()
    part2(almanac).println()
}

fun debugMapping(almanac: Almanac) {
    almanac.seeds.forEach {
        almanac.followMapping("seed" to it).joinToString(" -> ").println()
    }
}

fun part1(input: Almanac) = input.findLocations().minBy { it.second }
fun part2(input: Almanac) = input.findLocationsForSeedRange().minBy { it.second }

/*
 * Models
 */
data class RangeMapping(val sourceStart: Long, val destStart: Long, val length: Long)

class Mapping(val source: String, val destination: String, ranges: List<RangeMapping>) {
    val ranges = ranges.sortedBy(RangeMapping::sourceStart)

    init {
        val overlapping = this.ranges.zipWithNext().find {
            (r1, r2) ->
            r1.sourceStart + r1.length > r2.sourceStart
        }
        if (overlapping != null) println("WARNING: overlapping ranges found: $overlapping")
    }

    fun map(source: Long) : Long {
        var idx = ranges.binarySearchBy(source) { it.sourceStart }
        if (idx == -1 || idx < -ranges.size-1) return source
        if (idx < 0) idx = -idx - 2 // get floor entry
        val range = ranges[idx]
        val deltaSource = source - range.sourceStart
        return if (deltaSource < range.length) range.destStart + deltaSource else source
    }
}

data class Almanac(val seeds: List<Long>, val mappingsBySource: Map<String, Mapping>) {

    fun findLocations() : Sequence<Pair<Long, Long>> {
        return seeds.asSequence().map {
            val (label, value) = followMapping("seed" to it).last()
            check(label == "location") { "Label is $label" }
            it to value
        }
    }

    fun nextMapping(source: String, value: Long) : Pair<String, Long>? {
        return mappingsBySource[source]?.let { mapping ->
            mapping.destination to (mapping.map(value))
        }
    }

    fun followMapping(source: Pair<String, Long>) : Sequence<Pair<String, Long>> {
        return sequenceOf(source).expand { (source, value) ->
            val next = nextMapping(source, value)
            if (next == null) emptySequence() else sequenceOf(next)
        }
    }

    fun findLocationsForSeedRange() : Sequence<Pair<Long, Long>> {
        val total = (1..<seeds.size step 2).map { seeds[it] }.reduce(Long::plus)
        println("Total seeds: $total")
        var count = 0
        val finder = DynamicLocationFinder(this)
        return seeds.asSequence()
            .windowed(2, 2)
            .flatMap { (start, length) -> (start..<(start+length)).asSequence() }
            .map { it to finder.recurseToLocation("seed" to it) }
            .onEach { if (++count % 1_000_000 == 0) println("COMPUTED $count") }
    }
}

class DynamicLocationFinder(val almanac: Almanac, val maxSize: Int = 20_000_000, val cache : MutableMap<Pair<String, Long>, Long> = HashMap((maxSize * 1.5).toInt())) {

    fun recurseToLocation(sourceAndValue: Pair<String, Long>) : Long {
        var result = cache.get(sourceAndValue)
        if (result != null) return result
        val next = almanac.nextMapping(sourceAndValue.first, sourceAndValue.second) ?: throw IllegalStateException("Next mapping is null !")
        if (next.first == "location") return next.second
        result = recurseToLocation(next)
        cache[sourceAndValue] = result
        if (cache.size > maxSize) {
            cache.keys.filter { Random.nextDouble() < 0.3 }.forEach { cache.remove(it) }
        }
        return result
    }
}

/*
 * Parsing
 */
fun parse(input: List<String>) : Almanac {
    val iter = input.iterator()
    val seeds = Regex("\\d+").findAll(iter.next()).map { it.value.toLong() }.toList()

    val mappings = mutableMapOf<String, Mapping>()
    do {
        val mapping = parseMapping(iter) ?: break
        mappings[mapping.source] = mapping
    } while (iter.hasNext())

    return Almanac(seeds, mappings)
}

fun parseMapping(iter: Iterator<String>): Mapping? {
    var line: String
    while (iter.hasNext()) {
        line = iter.next()
        if (line.isBlank()) continue
        val match = Regex("(\\w+)-to-(\\w+) map:").matchEntire(line)
            ?: throw IllegalStateException("Should be on a new mapping")

        val (source, destination) = match.destructured
        val ranges = mutableListOf<RangeMapping>()
        while (iter.hasNext()) {
            line = iter.next()
            if (line.isBlank()) break
            val (dest, src, length) = Regex("(\\d+)\\s+(\\d+)\\s+(\\d+)").matchEntire(line)?.destructured
                ?: throw IllegalStateException("Line does not match range mapping: $line")
            ranges.add(RangeMapping(src.toLong(), dest.toLong(), length.toLong()))
        }
        return Mapping(source, destination, ranges)
    }
    return null
}
