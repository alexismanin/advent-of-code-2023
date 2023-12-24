import kotlin.math.max
import kotlin.math.min

inline fun <reified T> Matrix(nbRows: Int, nbCols: Int, fillValue: T) : Matrix<T> = Matrix(nbRows, nbCols, Array(
    Math.multiplyExact(
        nbRows,
        nbCols
    )
) { fillValue })

class Matrix<T>(val nbRows: Int, val nbCols: Int, private val content: Array<T>) {

    init {
        require(content.size == Math.multiplyExact(nbRows, nbCols)) { "Matrix buffer does not match matrix dimensions !" }
    }

    operator fun set(row: Int, col: Int, value: T) {
        content[index(row, col)] = value
    }

    private fun index(row: Int, col: Int) = Math.addExact(Math.multiplyExact(row, nbCols), col)

    operator fun get(row: Int, col: Int) : T = content[index(row, col)]

    operator fun get(location: Location) : Sequence<T> = sequence {
        val startIdx = location.row * nbCols + location.col
        for (i in startIdx..<startIdx+location.rowSpan) yield(content[i])
    }

    fun row(rowIdx: Int) : Array<T> {
        val startIdx = Math.multiplyExact(rowIdx, nbCols)
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
        val minRow = max(0, row - margin)
        val maxRow = min(nbRows, row + margin + 1)
        val minCol = max(0, col - margin)
        val maxCol = min(nbCols, col + span + margin)

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

    fun toString(valueToString: (T) -> String) : String {
        return (0..<nbRows).asSequence()
            .map {
                i ->
                (i*nbCols..<i*nbCols+nbCols)
                    .joinToString("") { j -> valueToString(content[j]) }
            }
            .joinToString(System.lineSeparator())
    }

    fun cross(center: Location) : Sequence<Location> = sequence {
        if (center.row > 0) yield(center.copy(row = center.row-1))
        if (center.col < nbCols-1) yield(center.copy(col = center.col+1))
        if (center.row < nbRows -1) yield(center.copy(row = center.row -1))
        if (center.col > 0) yield(center.copy(col = center.col -1))
    }

    /**
     * @return Values adjacent to the given row and column.
     */
    fun adjacent(location: Location, margin: Int = 1) : Sequence<Pair<Location, T>> {
        val (row, col, span) = location
        val minRow = max(0, row-margin)
        val maxRow = min(nbRows-1, row+margin)
        val minCol = max(0, col-margin)
        val locEndInclusive = col+(span-1)
        val maxCol = min(nbCols-1, locEndInclusive+margin)

        return sequence {
            // lines before location
            for (i in minRow..<row) {
                for (j in minCol..maxCol) yield(Location(i, j, 1) to get(i, j))
            }

            // same line, before location
            for (j in minCol..<col) yield(Location(row, j, 1) to get(row, j))
            // same line, after location
            if (maxCol > locEndInclusive) {
                for (j in locEndInclusive+1..maxCol) yield(Location(row, j, 1) to get(row, j))
            }

            // lines after location
            if (row < maxRow) {
                for (i in (row+1)..maxRow) {
                    for (j in minCol..maxCol) yield(Location(i, j, 1) to get(i, j))
                }
            }
        }
    }

    fun copy(): Matrix<T> {
        return Matrix(nbRows, nbCols, content.copyOf())
    }
}

data class Location(val row: Int, val col: Int, val rowSpan: Int = 1) : Comparable<Location> {
    override fun compareTo(other: Location): Int {
        var comparison = row - other.row
        if (comparison == 0) comparison = col - other.col
        if (comparison == 0) comparison = rowSpan - other.rowSpan
        return comparison
    }
}
