package xyz.missingnoshiny.ftg.node

import xyz.missingnoshiny.ftg.node.games.boggle.BoggleGame

fun main() {
    val dictionary = BoggleGame.frenchDictionary
    dictionary.forEach { println(it) }
    println(dictionary.getLetterFrequencies())
}
