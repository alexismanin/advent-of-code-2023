import java.lang.Math.addExact
import java.lang.Math.multiplyExact
import kotlin.math.min
import kotlin.math.max

fun main() {
    val input = readInput("Day03_test")
    val schema = parseSchematics(input)
    println(schema)
}

/*
 * Part solving functions
 */

fun part1() : Int {
    TODO()
}

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

fun Token.repr() = when (this) {
    is Dot -> "."
    is Figure -> "$value"
    is Symbol -> "$value"
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

    fun row(rowIdx: Int) : Array<T> {
        val startIdx = multiplyExact(rowIdx, nbCols)
        return content.copyOfRange(startIdx, startIdx + nbCols)
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
    fun adjacent(row: Int, col: Int) : Sequence<Token> {
        return sequence {
            for (r in max(0, row - 1)..min(row+1, content.nbRows-1)) {
                for (c in max(0, col - 1)..min(col+1, content.nbCols-1)) {
                    if (r != row || c != col) yield(content[r, c])
                }
            }
        }
    }

    fun recomposeNumber(x: Int, y: Int) : Int {
        content[x, y] as? Figure ?: throw IllegalArgumentException("No figure found at index ($x, $y)")
        TODO("Search other figures around in the same line, then concatenate them and parse concatenated text as integer")
    }
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
                in Array(10) { "$i"[0] } -> Figure("$char".toInt())
                else -> Symbol(char)
            }
            if (token != Dot) m[j, i] = token
        }
    }
    return Schematics(m)
}

/*
 * Utilities
 */
