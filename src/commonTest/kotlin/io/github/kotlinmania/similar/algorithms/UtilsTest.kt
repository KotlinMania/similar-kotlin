// port-lint: source algorithms/utils.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.asLookup
import kotlin.test.Test
import kotlin.test.assertEquals

class UtilsTest {
    @Test
    fun testUnique() {
        val u = unique(listOf('a', 'b', 'c', 'd', 'd', 'b').asLookup(), 0 until 6)
            .map { it.value() to it.originalIndex() }
        assertEquals(listOf('a' to 0, 'c' to 2), u)
    }

    @Test
    fun testIntHasher() {
        val ih = IdentifyDistinct(
            listOf("", "foo", "bar", "baz").asLookup(),
            1 until 4,
            listOf("", "foo", "blah", "baz").asLookup(),
            1 until 4,
        )
        assertEquals(0, ih.oldLookup()[1])
        assertEquals(1, ih.oldLookup()[2])
        assertEquals(2, ih.oldLookup()[3])
        assertEquals(0, ih.newLookup()[1])
        assertEquals(3, ih.newLookup()[2])
        assertEquals(2, ih.newLookup()[3])
        assertEquals(1 until 4, ih.oldRange())
        assertEquals(1 until 4, ih.newRange())
    }

    @Test
    fun testCommonPrefixLen() {
        assertEquals(
            0,
            commonPrefixLen(
                "".encodeToByteArray().asList().asLookup(),
                0 until 0,
                "".encodeToByteArray().asList().asLookup(),
                0 until 0,
            ),
        )
        assertEquals(
            7,
            commonPrefixLen(
                "foobarbaz".encodeToByteArray().asList().asLookup(),
                0 until 9,
                "foobarblah".encodeToByteArray().asList().asLookup(),
                0 until 10,
            ),
        )
        assertEquals(
            0,
            commonPrefixLen(
                "foobarbaz".encodeToByteArray().asList().asLookup(),
                0 until 9,
                "blablabla".encodeToByteArray().asList().asLookup(),
                0 until 9,
            ),
        )
        assertEquals(
            4,
            commonPrefixLen(
                "foobarbaz".encodeToByteArray().asList().asLookup(),
                3 until 9,
                "foobarblah".encodeToByteArray().asList().asLookup(),
                3 until 10,
            ),
        )
    }

    @Test
    fun testCommonSuffixLen() {
        assertEquals(
            0,
            commonSuffixLen(
                "".encodeToByteArray().asList().asLookup(),
                0 until 0,
                "".encodeToByteArray().asList().asLookup(),
                0 until 0,
            ),
        )
        assertEquals(
            4,
            commonSuffixLen(
                "1234".encodeToByteArray().asList().asLookup(),
                0 until 4,
                "X0001234".encodeToByteArray().asList().asLookup(),
                0 until 8,
            ),
        )
        assertEquals(
            0,
            commonSuffixLen(
                "1234".encodeToByteArray().asList().asLookup(),
                0 until 4,
                "Xxxx".encodeToByteArray().asList().asLookup(),
                0 until 4,
            ),
        )
        assertEquals(
            2,
            commonSuffixLen(
                "1234".encodeToByteArray().asList().asLookup(),
                2 until 4,
                "01234".encodeToByteArray().asList().asLookup(),
                2 until 5,
            ),
        )
    }
}
