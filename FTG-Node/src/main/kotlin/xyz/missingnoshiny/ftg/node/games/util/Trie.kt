package xyz.missingnoshiny.ftg.node.games.util

class Trie : Iterable<String> {
    data class Node(var word: String? = null, val children: MutableMap<Char, Node> = mutableMapOf())

    private val root = Node()

    fun insert(word: String) {
        var currentNode = root
        word.normalize().forEach {
            currentNode = currentNode.children.getOrPut(it) { Node() }
        }
        currentNode.word = word
    }

    fun insert(words: Sequence<String>) {
        words.forEach { insert(it) }
    }

    private fun getNode(word: String): Node? {
        var node = root
        word.forEach {
            node = node.children[it] ?: return null
        }
        return node
    }

    fun contains(word: String): Boolean {
        return getNode(word)?.word == word
    }

    fun hasPrefix(prefix: String): Boolean {
        return getNode(prefix)?.children?.isNotEmpty() ?: false
    }

    fun getFromKey(key: String): String? {
        return getNode(key)?.word
    }

    private fun getWords(currentNode: Node): Sequence<String> {
        return sequence {
            if (currentNode.word != null) yield(currentNode.word!!)
            currentNode.children.toSortedMap().forEach { (_, node) ->
                yieldAll(getWords(node))
            }
        }
    }

    override fun iterator(): Iterator<String> {
        return getWords(root).iterator()
    }
}