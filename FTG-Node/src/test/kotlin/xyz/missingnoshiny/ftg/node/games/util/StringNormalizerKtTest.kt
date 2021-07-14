package xyz.missingnoshiny.ftg.node.games.util

import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class StringNormalizerKtTest {

    @Test
    fun testNormalizationChange() {
        val string = "Tëst"
        assertNotEquals(string, string.normalize())
    }

    @Test
    fun testNormalization() {
        val string = "ça être réussi"
        assertEquals(string.normalize(), "ca etre reussi")
    }
}