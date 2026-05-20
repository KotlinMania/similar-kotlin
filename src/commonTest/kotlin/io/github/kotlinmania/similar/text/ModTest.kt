// port-lint: source text/mod.rs
package io.github.kotlinmania.similar.text

import io.github.kotlinmania.similar.ChangeTag
import io.github.kotlinmania.similar.DiffOp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModTest {
    @Test
    fun testCapturedOps() {
        val diff = TextDiff.fromLines(
            "Hello World\nsome stuff here\nsome more stuff here\n",
            "Hello World\nsome amazing stuff here\nsome more stuff here\n",
        )

        assertEquals(
            listOf(
                DiffOp.Equal(0, 0, 1),
                DiffOp.Replace(1, 1, 1, 1),
                DiffOp.Equal(2, 2, 1),
            ),
            diff.ops(),
        )
    }

    @Test
    fun testUnifiedDiff() {
        val diff = TextDiff.fromLines(
            "Hello World\nsome stuff here\nsome more stuff here\n",
            "Hello World\nsome amazing stuff here\nsome more stuff here\n",
        )
        assertTrue(diff.newlineTerminated())
        assertEquals(
            "--- old\n+++ new\n@@ -1,3 +1,3 @@\n Hello World\n-some stuff here\n+some amazing stuff here\n some more stuff here\n",
            diff.unifiedDiff().contextRadius(3).header("old", "new").toString(),
        )
    }

    @Test
    fun testLineOps() {
        val diff = TextDiff.fromLines(
            "Hello World\nsome stuff here\nsome more stuff here\n",
            "Hello World\nsome amazing stuff here\nsome more stuff here\n",
        )
        assertTrue(diff.newlineTerminated())
        assertEquals(
            listOf(
                ChangeTag.Equal to "Hello World\n",
                ChangeTag.Delete to "some stuff here\n",
                ChangeTag.Insert to "some amazing stuff here\n",
                ChangeTag.Equal to "some more stuff here\n",
            ),
            diff.iterAllChanges().toTagValues(),
        )
    }

    @Test
    fun testVirtualNewlines() {
        val diff = TextDiff.fromLines("a\nb", "a\nc\n")
        assertTrue(diff.newlineTerminated())
        assertEquals(
            listOf(
                ChangeTag.Equal to "a\n",
                ChangeTag.Delete to "b",
                ChangeTag.Insert to "c\n",
            ),
            diff.iterAllChanges().toTagValues(),
        )
    }

    @Test
    fun testCharDiff() {
        val diff = TextDiff.fromChars("Hello World", "Hallo Welt")
        assertEquals(
            listOf(
                DiffOp.Equal(0, 0, 1),
                DiffOp.Replace(1, 1, 1, 1),
                DiffOp.Equal(2, 2, 5),
                DiffOp.Replace(7, 2, 7, 1),
                DiffOp.Equal(9, 8, 1),
                DiffOp.Replace(10, 1, 9, 1),
            ),
            diff.ops(),
        )
    }

    @Test
    fun testRatio() {
        assertEquals(0.75f, TextDiff.fromChars("abcd", "bcde").ratio())
        assertEquals(1.0f, TextDiff.fromChars("", "").ratio())
    }

    @Test
    fun testGetCloseMatches() {
        assertEquals(
            listOf("apple", "ape"),
            getCloseMatches("appel", listOf("ape", "apple", "peach", "puppy"), 3, 0.6f),
        )
        assertEquals(
            listOf("aulo", "hulu", "uulo", "zulo"),
            getCloseMatches(
                "hulo",
                listOf("hi", "hulu", "hali", "hoho", "amaz", "zulo", "blah", "hopp", "uulo", "aulo"),
                5,
                0.7f,
            ),
        )
    }

    @Test
    fun testRegressionIssue37() {
        val diff = TextDiff.configure().diffLines("\u0018\n\n", "\n\n\r")
        assertEquals(
            "@@ -1 +1,0 @@\n-\u0018\n@@ -2,0 +2,2 @@\n+\n+\r",
            diff.unifiedDiff().contextRadius(0).toString(),
        )
    }
}

private fun Iterator<io.github.kotlinmania.similar.Change<String>>.toTagValues(): List<Pair<ChangeTag, String>> =
    buildList {
        while (hasNext()) {
            val change = next()
            add(change.tag() to change.value())
        }
    }
