package xyz.missingnoshiny.ftg.node.games.util

import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class TrieTest {

    @Test
    fun testInsertMultiple() {
        val trie = Trie()
        val stringSet = setOf("test", "quoi", "ok")
        trie.insert(stringSet.asSequence())
        assertEquals(stringSet, trie.toSet())
    }

    @Test
    fun testContainsTrue() {
        val trie = Trie()
        trie.insert(listOf("quoi", "hein").asSequence())
        assertTrue("quoi" in trie)
    }

    @Test
    fun testContainsFalse() {
        val trie = Trie()
        trie.insert(listOf("quoi", "hein").asSequence())
        assertFalse("test" in trie)
    }

    @Test
    fun testContainsEmpty() {
        val trie = Trie()
        trie.insert("test")
        assertFalse("" in trie)
    }
}