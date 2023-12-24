package day10

import Location
import Matrix
import day10.Direction.*
import println
import readInput

const val BOLD = "\u001B[1m"
const val GREEN = "\u001B[32m"
const val BLUE = "\u001B[34m"
const val RED="\u001B[31m"
const val RESET ="\u001B[0m"

fun main() {
    val input = readInput("Day10")
    val map = parse(input).identifyPath()

    map.println()
    part1(map).println()
}

fun part1(map: PipeMap) : Int {
    val nbMoves = map.path().count()
    return if (nbMoves % 2 == 0) nbMoves / 2 else nbMoves / 2 + 1
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
data class PipeTile(val pipe: Pipe, val onPath: Boolean = false) : Tile

fun Tile.repr() = when (this) {
    is Ground -> "."
    is Start -> "${BOLD}${GREEN}S${RESET}"
    is PipeTile -> if (onPath) "${RED}${pipe.name}${RESET}" else "${BLUE}${pipe.name}${RESET}"
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

    fun identifyPath(): PipeMap {
        val newTiles = tiles.copy()
        path().forEach { (loc, pipe) -> newTiles[loc.row, loc.col] = PipeTile(pipe, true) }
        return PipeMap(newTiles, start)
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
