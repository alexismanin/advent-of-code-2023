import kotlin.math.pow

val CAR_REGEX = Regex("(?i)card\\s*(?<id>\\d+)\\s*:(?<win>[\\s\\d]+)*\\|(?<draw>[\\s\\d]+)*")

fun main() {
    val input = readInput("Day04")
    val cards = input.map { parse(it) }
    part1(cards).println()
    part2(cards).println()
}

fun part1(cards : List<Card>) : Int {
    return cards
        .map { it.matches().count() }
        .sumOf { 2.0.pow(it - 1.0).toInt() }
}

fun part2(cards: List<Card>) : Int {
    // 1. Build number of matches for each card
    val matches = cards.associate { it.id to it.matches().count() }

    val occurrences = matches.asSequence()
        .map { (key, value) -> key to value }
        .expand {
            (id, count) ->
            if (count == 0) emptySequence()
            else (1..count)
                .asSequence()
                .mapNotNull {
                    i ->
                    val nextId = id + i
                    matches[nextId]?.let { nextId to it }
                }
        }

    return occurrences.count()
}

fun parse(card: String) : Card {
    val matcher = checkNotNull(CAR_REGEX.matchEntire(card)) { "Cannot parse input: [$card]"}
    val id = checkNotNull(matcher.groups["id"], { "id not found in [$card]" }).value.toInt()
    val win = matcher.groups["win"]?.value.toNumberList()
    val draw = matcher.groups["draw"]?.value.toNumberList()

    return Card(id, win, draw)
}

data class Card(val id: Int, val winning : List<Int>, val draw: List<Int>) {

    fun matches() : Sequence<Int> {
        val winSet = winning.toSet()

        check (winSet.size == winning.size) { "Winning cards contain doublon !" }

        return draw.asSequence().filter { winSet.contains(it) }
    }
}

fun String?.toNumberList() : List<Int> {
    check(this != null) { "No number list !" }
    return trim().splitToSequence(Regex("\\s+")).map { it.toInt() }.toList()
}
