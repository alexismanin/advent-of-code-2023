package day10

import Location
import Matrix
import day10.Direction.*
import readInput

fun main() {
    val input = readInput("Day10_test")
    val map = parse(input)

    println(map)
    println(map.path().joinToString(System.lineSeparator()))
}

enum class Direction(val rowMove: Int, val colMove: Int) {
    north(-1, 0), east(0, 1), south(1, 0), west(0, -1);
    fun opposite() = Direction.entries.find { it.rowMove - rowMove == 0 && it.colMove - colMove == 0 }
}

enum class Pipe(val p1: Direction, val p2: Direction) {
    `|`(north, south),
    `-`(west, east),
    L(north, east),
    J(north, west),
    `7`(south, west),
    F(south, east)
}

sealed interface Tile
data object Ground : Tile
data object Start: Tile
data class PipeTile(val pipe: Pipe) : Tile

fun Tile.repr() = when (this) {
    is Ground -> "."
    is Start -> "S"
    is PipeTile -> pipe.name
}

fun Location.move(direction: Direction) = copy(row + direction.rowMove, col + direction.colMove)

class PipeMap(val tiles: Matrix<Tile>, val start: Location) {

    override fun toString(): String = tiles.toString(Tile::repr)

    fun path() : Sequence<Pair<Location, Pipe>> {
        val pathStart = tiles.cross(start)
            .map { it to tiles[it].first() }
            .first { (_, tile) -> tile is PipeTile }

        return generateSequence(start to pathStart.let { it.first to (it.second as PipeTile).pipe }) {
            (previous, current) ->
                val nextLoc : Location = if (current.first.move(current.second.p1) == previous)
                    current.first.move(current.second.p2)
                else current.first.move(current.second.p1)

                when (val t = tiles[nextLoc].first()) {
                    is Start -> null
                    is PipeTile -> current.first to (nextLoc to t.pipe)
                    else -> throw IllegalStateException("UNEXPECTED: SHOULD BE A PIPE !")
                }
            }
            .map { it.second }
    }
}

fun parse(input: List<String>) : PipeMap {
    val tiles = Matrix<Tile>(input.size, input[0].length, Ground)
    var startLocation : Location? = null
    for (i in 0..<tiles.nbRows) {
        val line = input[i]
        for (j in 0..<tiles.nbCols) {
            when (val letter = line[j]) {
                '.' -> { /* Ground; Already set as default value */ }
                'S' -> {
                    check(startLocation == null) { "A start position has already been found at $startLocation. Current position: ($i, $j)" }
                    tiles[i, j] = Start
                    startLocation = Location(i, j)
                }
                else -> tiles[i, j] = PipeTile(Pipe.valueOf("$letter"))
            }
        }
    }

    requireNotNull(startLocation) { "No start position found !" }
    return PipeMap(tiles, startLocation)
}
