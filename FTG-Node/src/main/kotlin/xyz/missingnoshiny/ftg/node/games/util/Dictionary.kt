package xyz.missingnoshiny.ftg.node.games.util

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.writeLines

class Dictionary(private val constraint: DictionaryConstraint): Iterable<String> {
    companion object {
        fun fromFile(file: File, constraint: DictionaryConstraint): Dictionary {
            val dictionary = Dictionary(constraint)
            file.useLines { dictionary.insert(it) }
            return dictionary
        }

        fun fromStream(stream: InputStream, constraint: DictionaryConstraint): Dictionary {
            val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
            val dictionary = Dictionary(constraint)
            while (reader.ready()) {
                val line = reader.readLine() ?: break
                dictionary.insert(line)
            }
            return dictionary
        }
    }

    private val words = Trie()
    private var modified = true
    private var letterFrequencies = getLetterFrequencies()

    fun insert(word: String) {
        if (constraint.isValid(word)) words.insert(word)
        modified = true
    }

    fun insert(wordList: Sequence<String>) {
        words.insert(wordList.filter { constraint.isValid(it) })
        modified = true
    }

    fun getLetterFrequencies(): Map<Char, Float> {
        if (modified) {
            val map = mutableMapOf<Char, Int>()
            words.forEach { word -> word.normalize().forEach { map.merge(it, 1, Int::plus) }}
            val sum = map.values.sum()
            letterFrequencies = map.mapValues { it.value.toFloat() / sum }
            modified = false
        }
        return letterFrequencies
    }

    fun contains(word: String) = constraint.isValid(word) or (word in words)

    fun hasPrefix(prefix: String) = words.hasPrefix(prefix)

    fun getFromKey(key: String) = words.getFromKey(key)

    override fun iterator(): Iterator<String> {
        return words.iterator()
    }

    fun writeToFile(path: Path) {
        path.writeLines(this)
    }
}