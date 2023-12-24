import kotlin.math.min
import kotlin.math.max

val FIGURES = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

fun main() {
    val input = readInput("Day03")
    val schema = parseSchematics(input)
    part1(schema).println()
    part2(schema).println()
}

/*
 * Part solving functions
 */

fun part1(schema: Schematics) : Int = schema.findNumbers(true)
                                            .map { it.second }
                                            .sum()

fun part2(schema: Schematics) : Long {
    return schema.findNumbers(true)
          .flatMap { (loc, n) ->
              schema.adjacent(loc)
                  .mapNotNull { (loc, token) -> if (token is Symbol && token.value == '*') loc to n else null }
          }
        .groupBy({ it.first }, {it.second })
        .values
        .asSequence()
        .filter { it.size == 2 }
        .map { it[0].toLong() * it[1] }
        .sum()
}

/*
 * Schematics modeling
 */
sealed interface Token
data object Dot : Token
data class Figure(val value: Int) : Token
data class Symbol(val value: Char) : Token

class Schematics(private val content: Matrix<Token>) {

    override fun toString(): String {
        val nl = System.lineSeparator()
        return "Schematics:$nl${content.toString(Token::repr)}"
    }

    fun adjacent(loc: Location, margin : Int = 1) = content.adjacent(loc, margin)

    fun findNumbers(partsOnly: Boolean) : Sequence<Pair<Location, Int>> {
        var numberLocations = content.findMatches(true) { it is Figure }
         if (partsOnly) numberLocations = numberLocations.filter {
             adjacent(it).any { (_, token) -> token is Symbol }
         }

        return numberLocations.map {
            loc ->
            val number = content[loc]
                .map { (it as Figure).value }
                .joinToString("")
                .toInt()
            loc to number
        }
    }

    fun subset(location: Location, margin: Int = 1) : Schematics {
        return Schematics(content.subset(location, margin))
    }
}

/*
 * Utilities
 */

fun Token.repr() = when (this) {
    is Dot -> "."
    is Figure -> "$value"
    is Symbol -> "$value"
}

fun parseSchematics(input: List<String>) : Schematics {
    require(input.isNotEmpty()) { "Input is empty !"}
    val nbRows = input.size
    val nbCols = input[0].length
    val m = Matrix<Token>(nbRows, nbCols, Dot)
    for (j in 0..< nbRows) {
        val line = input[j]
        for (i in 0..< nbCols) {
            val token = when (val char = line[i]) {
                '.' -> Dot
                in FIGURES -> Figure("$char".toInt())
                else -> Symbol(char)
            }
            if (token != Dot) m[j, i] = token
        }
    }
    return Schematics(m)
}
