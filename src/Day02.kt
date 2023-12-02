import java.lang.Exception
import java.util.*
import Color.*

typealias ColorCounts=Map<Color, Int>

val GAME_PREFIX = Regex("(?i)game\\s*(?<id>\\d+):")
val COLOR_COUNT = Regex("(?<count>\\d+)\\s*(?<color>red|green|blue)")

fun main() {
    val input = readInput("Day02")
    val games = input.map(::parseGame)
    part1(games, EnumMap(mapOf(red to 12, green to 13, blue to 14)))
    part2(games)
}

fun part1(games: List<Game>, maxCapacity: ColorCounts) {
    games.asSequence()
         .filter { game ->  game.isPossible(maxCapacity) }
         .map(Game::id)
         .sum()
         .println()
}

fun part2(games: List<Game>) {
    games.asSequence()
         .map(Game::minimumSet)
         .map(ColorCounts::power)
         .sum()
        .println()
}

fun ColorCounts.power() = values.reduce(Math::multiplyExact).toLong()

fun Game.minimumSet() : ColorCounts {
    val acc = EnumMap<Color, Int>(Color::class.java)
    Color.entries.forEach {
        color
        ->
        val colorMax = draws.maxOf { it[color] ?: 0 }
        if (colorMax > 0) acc.merge(color, colorMax) { _, _ -> throw IllegalStateException("Doublon color found: $color") }
    }
    return Collections.unmodifiableMap(acc)
}

fun Game.isPossible(maxCapacity: ColorCounts) : Boolean {
    return Color.entries.all {
        color
        ->
        draws.all {
            draw
            ->
            (draw[color] ?: 0) <= (maxCapacity[color] ?: 0)
        }
    }
}

fun parseGame(input: String) : Game {
    val prefixMatch = checkNotNull(GAME_PREFIX.find(input)) { "Game prefix not found: $input" }
    val id = checkNotNull(prefixMatch.groups["id"]?.value?.toInt()) { "Game ID not found: $input" }
    val stripIdx = prefixMatch.range.last + 1
    val drawsTxt = input.substring(stripIdx)
    val draws = drawsTxt
        .split(';')
        .map(::parseDraw)
        .toList()
    return Game(id, draws)
}

fun parseDraw(input: String) : ColorCounts {
    return COLOR_COUNT.findAll(input)
        .map {
            match
            ->
            val count = checkNotNull(match.groups["count"]?.value?.toInt()) { "Count not find count in ${match.value}" }
            val color = checkNotNull(match.groups["color"]?.value) { "Could not find color in ${match.value}"}
            try {
                Color.valueOf(color) to requirePositive(count)
            } catch (e: Exception) {
                throw IllegalStateException("Cannot parse ${match.value}", e)
            }
        }
        .fold(EnumMap<Color, Int>(Color::class.java)) {
            map, color
            ->
            map.merge(color.first, color.second, Int::plus)
            map
        }
        .let { Collections.unmodifiableMap(it) }
}

data class Game(val id: Int, val draws: List<ColorCounts>)

enum class Color { red, green, blue }

fun requirePositive(value: Int) : Int {
    require(value >= 0) { "Positive value expected" }
    return value
}