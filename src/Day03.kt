import java.lang.Math.addExact
import java.lang.Math.multiplyExact
import kotlin.math.min
import kotlin.math.max

val FIGURES = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

fun main() {
    val input = readInput("Day03")
    val schema = parseSchematics(input)
    part1(schema).println()
}

/*
 * Part solving functions
 */

fun part1(schema: Schematics) : Int = schema.findNumbers(true)
                                            .onEach { schema.subset(it.first).println() }
                                            .map { it.second }
                                            .sum()

fun part2() : Int {
    TODO()
}

/*
 * Schematics modeling
 */
sealed interface Token
data object Dot : Token
data class Figure(val value: Int) : Token
data class Symbol(val value: Char) : Token


data class Location(val row: Int, val col: Int, val span: Int = 1) : Comparable<Location> {
    override fun compareTo(other: Location): Int {
        var comparison = row - other.row
        if (comparison == 0) comparison = col - other.col
        if (comparison == 0) comparison = span - other.span
        return comparison
    }
}

inline fun <reified T> Matrix(nbRows: Int, nbCols: Int, fillValue: T) : Matrix<T> = Matrix(nbRows, nbCols, Array(multiplyExact(nbRows, nbCols)) { fillValue })
class Matrix<T>(val nbRows: Int, val nbCols: Int, private val content: Array<T>) {

    init {
        require(content.size == multiplyExact(nbRows, nbCols)) { "Matrix buffer does not match matrix dimensions !" }
    }

    operator fun set(row: Int, col: Int, value: T) {
        content[index(row, col)] = value
    }

    private fun index(row: Int, col: Int) = addExact(multiplyExact(row, nbCols), col)

    operator fun get(row: Int, col: Int) : T = content[index(row, col)]

    operator fun get(location: Location) : Sequence<T> = sequence {
        val startIdx = location.row * nbCols + location.col
        for (i in startIdx..<startIdx+location.span) yield(content[i])
    }

    fun row(rowIdx: Int) : Array<T> {
        val startIdx = multiplyExact(rowIdx, nbCols)
        return content.copyOfRange(startIdx, startIdx + nbCols)
    }

    fun findMatches(mergeContiguous: Boolean, predicate: (T) -> Boolean) : Sequence<Location> {
        if (!mergeContiguous) {
            return sequence {
                for (row in 0..<nbRows) {
                    for (col in 0..<nbCols) {
                        if (predicate(get(row, col))) yield(Location(row, col))
                    }
                }
            }
        }

        return sequence {
            for (row in 0..<nbRows) {
                var matchStart: Int? = null
                for (col in 0..<nbCols) {
                    val matches = predicate(get(row, col))
                    if (matches && matchStart == null) matchStart = col
                    else if (!matches && matchStart != null) {
                        yield(Location(row, matchStart, col - matchStart))
                        matchStart = null
                    }
                }
                if (matchStart != null) yield(Location(row, matchStart, nbCols - matchStart))
            }
        }
    }

    fun subset(location: Location, margin: Int): Matrix<T> {
        val (row, col, span) = location
        val minRow = max(0, row-margin)
        val maxRow = min(nbRows, row+margin + 1)
        val minCol = max(0, col-margin)
        val maxCol = min(nbCols, col+span+margin)

        val subsetNCols = maxCol - minCol
        val subsetNRows = maxRow - minRow
        val startIdx = minRow*nbCols+minCol
        val subset = content.copyOfRange(startIdx, startIdx +(subsetNRows * subsetNCols))
        var offset = 0
        for (i in minRow+1..<maxRow) {
            offset += subsetNCols
            content.copyInto(subset, offset, i*nbCols+minCol, i*nbCols+maxCol)
        }

        return Matrix(subsetNRows, subsetNCols, subset)
    }
}

class Schematics(private val content: Matrix<Token>) {

    override fun toString(): String {
        val nl = System.lineSeparator()
        return (0..<content.nbRows).joinToString(nl, "Schematics:$nl") {
            x ->
            content.row(x).joinToString("") { token -> token.repr() }
        }
    }

    /**
     * @return Values adjacent to the given row and column.
     */
    fun adjacent(location: Location, margin: Int = 1) : Sequence<Token> {
        val (row, col, span) = location
        val minRow = max(0, row-margin)
        val maxRow = min(content.nbRows-1, row+margin)
        val minCol = max(0, col-margin)
        val locEndInclusive = col+(span-1)
        val maxCol = min(content.nbCols-1, locEndInclusive+margin)

        return sequence {
            // lines before location
            for (i in minRow..<row) {
                for (j in minCol..maxCol) yield(content[i, j])
            }

            // same line, before location
            for (j in minCol..<col) yield(content[row, j])
            // same line, after location
            if (maxCol > locEndInclusive) {
                for (j in locEndInclusive+1..maxCol) yield(content[row, j])
            }

            // lines after location
            if (row < maxRow) {
                for (i in (row+1)..maxRow) {
                    for (j in minCol..maxCol) yield(content[i, j])
                }
            }
        }
    }

    fun findNumbers(partsOnly: Boolean) : Sequence<Pair<Location, Int>> {
        var numberLocations = content.findMatches(true) { it is Figure }
         if (partsOnly) numberLocations = numberLocations.filter {
             adjacent(it).any { token -> token is Symbol }
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
