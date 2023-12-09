package day09

import println
import java.util.function.LongBinaryOperator

import readInput

private typealias ValueReport=LongArray
private typealias OasisReport=List<LongArray>

fun main() {
    val input = readInput("Day09")
    val report = parse(input)
    // debug(report)
    part1(report).println()
    part2(report).println()
}

fun part1(report: OasisReport) = report.map { predictNextValue(it) }.sum()
fun part2(report: OasisReport) = report.map { predictPreviousValue(it) }.sum()


fun debug(report: OasisReport) {
    val nl = System.lineSeparator()
    val reducer = ReductionSteps({ v1, v2 -> v2 - v1 }, { values -> values.all { it == 0L }})
    report.joinToString(nl) {
        vr ->
        val entireReduction = sequenceOf(vr) + reducer.reduce(vr)
        entireReduction
            .mapIndexed { idx, it -> it.joinToString("\t", prefix = "\t".repeat(idx+1)) }
            .joinToString(nl)
    }.println()
}

fun parse(input: List<String>): OasisReport {
    val whitespaces = Regex("\\s+")
    return input.mapNotNull {
        line ->
        if (line.isBlank()) null
        else {
            val tokens = line.split(whitespaces)
            ValueReport(tokens.size) { i -> tokens[i].toLong() }
        }
    }
}

class ReductionSteps(
    val reducer: LongBinaryOperator = LongBinaryOperator { v1, v2 -> v2 - v1 },
    val stopCondition: (LongArray) -> Boolean = { it.all { value -> value == 0L } }
) {
    fun reduce(input: LongArray) : Sequence<LongArray> = sequence {
        var step = input
        do {
            step = LongArray(step.size - 1) {
                i ->
                reducer.applyAsLong(step[i], step[i+1])
            }
            yield(step)
        } while (!stopCondition(step))
    }
}

fun predictNextValue(report: ValueReport) : Long {
    return report.last() + ReductionSteps()
        .reduce(report)
        .map { it.last() }
        .sum()
}

fun predictPreviousValue(report: ValueReport) : Long {
    val firsts = ReductionSteps()
        .reduce(report)
        .map { it.first() }
        .toList()

    val factor = firsts.foldRight(0L, Long::minus)
    return report.first() - factor
}
