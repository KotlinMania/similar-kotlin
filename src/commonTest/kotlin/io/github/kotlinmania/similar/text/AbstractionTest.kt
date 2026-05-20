// port-lint: source text/abstraction.rs
package io.github.kotlinmania.similar.text

import kotlin.test.Test
import kotlin.test.assertEquals

class AbstractionTest {
    @Test
    fun testSplitLines() {
        assertEquals(
            listOf("first\n", "second\r", "third\r\n", "fourth\n", "last"),
            "first\nsecond\rthird\r\nfourth\nlast".asDiffableStr().tokenizeLines(),
        )
    }

    @Test
    fun testSplitWords() {
        assertEquals(
            listOf("foo", "    ", "bar", " ", "baz", "\n\n  ", "aha"),
            "foo    bar baz\n\n  aha".asDiffableStr().tokenizeWords(),
        )
    }

    @Test
    fun testSplitChars() {
        assertEquals(
            listOf("a", "b", "c", "f", "\u00f6", "\u2744", "\ufe0f"),
            "abcf\u00f6\u2744\ufe0f".asDiffableStr().tokenizeChars(),
        )
    }

    @Test
    fun testSplitGraphemes() {
        assertEquals(
            listOf("a", "b", "c", "f", "\u00f6", "\u2744", "\ufe0f"),
            "abcf\u00f6\u2744\ufe0f".asDiffableStr().tokenizeGraphemes(),
        )
    }
}
