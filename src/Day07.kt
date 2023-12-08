private typealias Hand = List<GameCard>
private typealias GroupedHand = Map<GameCard, Int>
private typealias CamelGame = List<Pair<Hand, Int>>

fun main() {
    val input = readInput("Day07")
    val game = parseGame(input)
    solveGame(game, false)
    solveGame(game, true)
}

private fun solveGame(game: CamelGame, jIsForJoker: Boolean) {
    val handCompare = HandComparator(jIsForJoker)
    val sortedGame = game.sortedWith { h1, h2 -> handCompare.compare(h1.first, h2.first) }
    var total = 0L
    for (rank in 1..sortedGame.size) {
        total += rank.toLong() * sortedGame[rank-1].second
    }
    println(total)
}

private enum class GameCard(val symbol: Char, val power: Int) {
    As   ('A', 20),
    King ('K', 13),
    Queen('Q', 12),
    Jack ('J', 11),
    Ten  ('T', 10),
    Nine ('9',  9),
    Eight('8',  8),
    Seven('7',  7),
    Six  ('6',  6),
    Five ('5',  5),
    Four ('4',  4),
    Three('3',  3),
    Two  ('2',  2),
}

private enum class HandType(val power: Int, val match: (GroupedHand) -> Boolean) {
    FiveOfAKind(7, { hand -> hand.values.max() == 5 }),
    FourOfAKind(6, { hand -> hand.values.max() == 4 }),
    FullHouse(5, { hand -> hand.values.distinct().containsAll(listOf(2, 3)) }),
    ThreeOfAKind(4, { hand -> hand.values.max() == 3 }),
    TwoPair(3, { hand -> hand.values.countDistinct()[2] == 2 }),
    OnePair(2, { hand -> hand.values.countDistinct().let { it[2] == 1 && it[1] == 3 } }),
    HighCard(1, { hand -> hand.values.all { it == 1 }})
}

private class HandComparator(val jIsForJoker: Boolean) : Comparator<Hand> {
    private val types = HandType.entries.sortedBy { -it.power }

    override fun compare(hand1: Hand, hand2: Hand) : Int {
        check(hand1.size == 5 && hand2.size == 5)
        var gh1 = hand1.countDistinct()
        var gh2 = hand2.countDistinct()
        if (jIsForJoker) {
            gh1 = findBest(gh1)
            gh2 = findBest(gh2)
        }
        val type1 = types.first { it.match(gh1) }
        val type2 = types.first { it.match(gh2) }

        val typeComparison = type1.power - type2.power
        if (typeComparison != 0) return typeComparison

        val comparisonByCard =  hand1.powers(jIsForJoker).zip(hand2.powers(jIsForJoker)) { c1, c2 -> c1 - c2 }
        return comparisonByCard.firstOrNull { it != 0 } ?: 0
    }

    private fun findBest(hand: GroupedHand) : GroupedHand {
        var jockerCount = hand[GameCard.Jack] ?: 0
        if (jockerCount < 1 || jockerCount >= 5) return hand
        val edited : MutableMap<GameCard, Int> = (hand - GameCard.Jack).toMutableMap()
        val (maxKey, _) = edited.maxBy { it.value }
        edited.merge(maxKey, jockerCount, Int::plus)
        return edited
    }
}

fun <T> Collection<T>.countDistinct() = asSequence()
    .groupingBy { it }
    .eachCount()

private fun Hand.powers(jIsForJoker: Boolean) = asSequence().map { if (it == GameCard.Jack && jIsForJoker) 1 else it.power }

private fun parseGame(input: List<String>) : CamelGame {
    return input.map {
        line ->
        val hand : Hand = line.subSequence(0, 5).map {
            card ->
            GameCard.entries.find {
                it.symbol == card
            }!!
        }
        val bid = line.substring(6).toInt()
        hand to bid
    }
}