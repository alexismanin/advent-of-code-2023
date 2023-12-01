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
fun Regex.findLast(input: CharSequence) = findAll(input).lastOrNull()

/**
 * Try to fetch value of the first group in this match. Raise an error if it does not exist.
 */
fun MatchResult?.firstGroup() = checkNotNull(this?.groupValues?.getOrNull(1)) {
    "Regex group detection failed. Time to search for a bug !"
}

fun main() {
    fun List<String>.decipher(
        firstDigitRegex: Regex,
        lastDigitRegex: Regex?,
        mappingTable: Map<String, String>?
    ) : Int = fold(0) {
        sum, line
        ->
        val firstDigit = firstDigitRegex.find(line).firstGroup().let { mappingTable?.get(it) ?: it }
        val lastDigit = (lastDigitRegex?.find(line) ?: firstDigitRegex.findLast(line)).firstGroup().let { mappingTable?.get(it) ?: it }
        sum + "$firstDigit$lastDigit".toInt()
    }

    fun part1(input: List<String>) = input.decipher(Regex("(\\d)"), Regex("(\\d)[^\\d]*$"), null)

    fun part2(input: List<String>) = input.decipher(Regex("(\\d|one|two|three|four|five|six|seven|eight|nine)"), null, TXT_FIGURES)

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}
