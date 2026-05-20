// port-lint: source utils.rs
package io.github.kotlinmania.similar

import io.github.kotlinmania.similar.text.asDiffableStr
import kotlin.test.Test
import kotlin.test.assertEquals

class UtilsTest {
    @Test
    fun testRemapper() {
        val text = "foo bar baz"
        val words = text.asDiffableStr().tokenizeWords()
        val remap = TextDiffRemapper.new(words, words, text, text)

        assertEquals("foo bar", remap.sliceOld(0 until 3))
        assertEquals(" bar", remap.sliceOld(1 until 3))
        assertEquals("foo", remap.sliceOld(0 until 1))
        assertEquals("foo bar baz", remap.sliceOld(0 until 5))
        assertEquals(null, remap.sliceOld(0 until 6))
    }
}
