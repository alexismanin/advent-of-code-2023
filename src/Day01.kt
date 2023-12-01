val TXT_FIGURES = mapOf(
    "one"   to "1",
    "two"   to "2",
    "three" to "3",
    "four"  to "4",
    "five"  to "5",
    "six"   to "6",
    "seven" to "7",
    "eight" to "8",
    "nine"  to "9"
)

/**
 * Find last occurrence of this regex in given input text.
 * @return `null` if we cannot find any occurrence.
 */
fun Regex.findLast(input: CharSequence, startIndex: Int = 0) : MatchResult? {
    var lastFind = findAll(input, startIndex).lastOrNull() ?: return null
    // Safety: if two patterns overlap, detection sequence might have failed to detect it.
    // This loop search for such patterns and get the real last pattern.
    var i = lastFind.range.first + 1
    val end = lastFind.range.last
    do {
        val next = find(input, i)
        if (next != null) {
            lastFind = next
            i = next.range.first
        }
    } while (i++ <= end)
    return lastFind
}

/**
 * Try to fetch value of the first group in this match. Raise an error if it does not exist.
 */
fun MatchResult?.firstGroup() = this?.groupValues?.getOrNull(1)

fun main() {
    fun List<String>.decipher(
        firstDigitRegex: Regex,
        lastDigitRegex: Regex?,
        mappingTable: Map<String, String>?
    ) : Int = fold(0) {
        sum, line
        ->
        val firstFind = checkNotNull(firstDigitRegex.find(line))
        val nextStartIdx = firstFind.range.last +1
        val firstDigit = checkNotNull(firstFind.firstGroup(), {"first digit not found"}).let { mappingTable?.get(it) ?: it }
        val lastDigit = (lastDigitRegex?.find(line, nextStartIdx) ?: firstDigitRegex.findLast(line, nextStartIdx)).firstGroup()
            ?.let { mappingTable?.get(it) ?: it }
            ?: firstDigit
        sum + "$firstDigit$lastDigit".toInt()
    }

    fun part1(input: List<String>) = input.decipher(Regex("(\\d)"), Regex("(\\d)[^\\d]*$"), null)

    fun part2(input: List<String>) = input.decipher(Regex("(\\d|one|two|three|four|five|six|seven|eight|nine)"), null, TXT_FIGURES)

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}
