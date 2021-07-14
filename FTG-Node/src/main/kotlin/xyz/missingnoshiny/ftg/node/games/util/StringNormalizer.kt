package xyz.missingnoshiny.ftg.node.games.util

import java.text.Normalizer

fun String.normalize(): String {
    var string = Normalizer.normalize(this, Normalizer.Form.NFD)
    string = Regex("\\p{InCombiningDiacriticalMarks}+").replace(string, "")
    return string
}
