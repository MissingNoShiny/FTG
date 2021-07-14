package xyz.missingnoshiny.ftg.node.games.util

import kotlin.random.Random

class WeightedRandomCharSequence(private val weights: Map<Char, Float>): Sequence<Char> {

    init {
        if (weights.isEmpty()) throw IllegalArgumentException("weights can't be empty")
    }

    override fun iterator(): Iterator<Char> = sequence {
        val (sortedChars, sortedWeights) = weights.toList().sortedBy { (_, weight) -> weight }.unzip()
        val cumulativeWeights = sortedWeights.scan(0f) { acc, weight -> acc + weight }.drop(1)
        while (true) {
            val random = Random.nextFloat() * cumulativeWeights.last()
            yield(sortedChars[cumulativeWeights.indexOfFirst { it > random }])
        }
    }.iterator()
}