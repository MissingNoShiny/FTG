package xyz.missingnoshiny.ftg.node.games.util

fun interface DictionaryConstraint {
    fun isValid(word: String): Boolean
}