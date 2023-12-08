fun main() {
    val input = readInput("Day06")
    // val input = readInput("Day06_test")
    val races = parse(input)
    part1(races)
    part2(parseCorrectly(input).also { println(it) })
}

fun part1(races : Sequence<Race>) = races.map{ it.beats().count() }.reduce(Math::multiplyExact).println()
fun part2(race: Race) = race.beats().count().println()

data class Race(val time: Long, val record: Long) {
    fun choices() = (0L..time).asSequence()
        .map { hold -> hold to hold * (time - hold) }
    fun beats() = choices().filter { it.second > record }
}


private val WHITESPACES = Regex("\\s+")

fun parse(input: List<String>) : Sequence<Race> {
    require(input.size == 2) { "Race parsing requires exactly two lines !" }
    val (timeTxt, distTxt) = input
    val whitespaces = WHITESPACES
    val times = timeTxt.substring(5).splitToSequence(whitespaces).filter(String::isNotBlank).map { it.toLong() }
    val dists = distTxt.substring(9).splitToSequence(whitespaces).filter(String::isNotBlank).map { it.toLong() }

    return times.zip(dists) { time, dist -> Race(time, dist) }
}

fun parseCorrectly(input: List<String>): Race {
    require(input.size == 2) { "Race parsing requires exactly two lines !" }
    val (timeTxt, distTxt) = input
    val time = timeTxt.substring(5).replace(WHITESPACES, "").toLong()
    val dist = distTxt.substring(9).replace(WHITESPACES, "").toLong()
    return Race(time, dist)
}