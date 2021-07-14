package xyz.missingnoshiny.ftg.node.games.boggle

import xyz.missingnoshiny.ftg.node.games.util.Dictionary
import xyz.missingnoshiny.ftg.node.games.util.Trie
import xyz.missingnoshiny.ftg.node.games.util.WeightedRandomCharSequence

class BoggleGrid(private val size: Int, private val dictionary: Dictionary) {
    data class Tile(val value: Char = ('a'..'z').random())

    private val randomCharSequence = WeightedRandomCharSequence(dictionary.getLetterFrequencies())
    private val tiles = Array(size) {
        Array(size) {
            Tile(randomCharSequence.first())
        }
    }

    val solutions = solve()

    private fun findWordsRec(x: Int, y: Int, currentWord: String, visited: Array<BooleanArray>, foundWords: Trie) {
        val word = dictionary.getFromKey(currentWord)
        if (word != null) foundWords.insert(word)
        visited[x][y] = true
        for (x2 in (x - 1).coerceAtLeast(0) until (x + 2).coerceAtMost(size)) {
            for (y2 in (y - 1).coerceAtLeast(0) until (y + 2).coerceAtMost(size)) {
                if (visited[x2][y2]) continue
                val newWord = currentWord + tiles[x2][y2].value
                if (dictionary.hasPrefix(newWord)) findWordsRec(x2, y2, newWord, visited, foundWords)
            }
        }
        visited[x][y] = false
    }

    private fun solve(): Trie {
        val visited = Array(size) { BooleanArray(size) { false } }
        val solutions = Trie()

        tiles.forEachIndexed { x, line ->
            for (y in line.indices) {
                findWordsRec(x, y, "", visited, solutions)
            }
        }

        return solutions
    }

    override fun toString(): String {
        return tiles.joinToString("\n") { line -> line.map { it.value }.joinToString(" ") }
    }
}