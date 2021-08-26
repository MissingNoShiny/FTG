package xyz.missingnoshiny.ftg.node.games.boggle

import xyz.missingnoshiny.ftg.node.games.util.Trie

fun getScore(word: String): Int = when (word.length) {
        3 -> 1
        4 -> 1
        5 -> 2
        6 -> 3
        7 -> 5
        else -> 8
}

fun Trie.calculateScore() = this.asSequence().map { getScore(it) }.sum()