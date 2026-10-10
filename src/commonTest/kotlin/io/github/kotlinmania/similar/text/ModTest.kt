// port-lint: source text/mod.rs
package io.github.kotlinmania.similar.text

import io.github.kotlinmania.similar.Change
import io.github.kotlinmania.similar.ChangeTag
import io.github.kotlinmania.similar.DiffOp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModTest {
    @Test
    fun testCapturedOps() {
        val diff =
            TextDiff.fromLines(
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
    fun testCapturedWordOps() {
        val diff =
            TextDiff.fromWords(
                "Hello World\nsome stuff here\nsome more stuff here\n",
                "Hello World\nsome amazing stuff here\nsome more stuff here\n",
            )
        val changes =
            diff.ops().flatMap { op ->
                diff.iterChanges(op).asSequence().toList()
            }
        assertEquals(
            listOf(
                Change(ChangeTag.Equal, 0, 0, "Hello"),
                Change(ChangeTag.Equal, 1, 1, " "),
                Change(ChangeTag.Equal, 2, 2, "World"),
                Change(ChangeTag.Equal, 3, 3, "\n"),
                Change(ChangeTag.Equal, 4, 4, "some"),
                Change(ChangeTag.Equal, 5, 5, " "),
                Change(ChangeTag.Insert, null, 6, "amazing"),
                Change(ChangeTag.Insert, null, 7, " "),
                Change(ChangeTag.Equal, 6, 8, "stuff"),
                Change(ChangeTag.Equal, 7, 9, " "),
                Change(ChangeTag.Equal, 8, 10, "here"),
                Change(ChangeTag.Equal, 9, 11, "\n"),
                Change(ChangeTag.Equal, 10, 12, "some"),
                Change(ChangeTag.Equal, 11, 13, " "),
                Change(ChangeTag.Equal, 12, 14, "more"),
                Change(ChangeTag.Equal, 13, 15, " "),
                Change(ChangeTag.Equal, 14, 16, "stuff"),
                Change(ChangeTag.Equal, 15, 17, " "),
                Change(ChangeTag.Equal, 16, 18, "here"),
                Change(ChangeTag.Equal, 17, 19, "\n"),
            ),
            changes,
        )
    }

    @Test
    fun testUnifiedDiff() {
        val diff =
            TextDiff.fromLines(
                "Hello World\nsome stuff here\nsome more stuff here\n",
                "Hello World\nsome amazing stuff here\nsome more stuff here\n",
            )
        assertTrue(diff.newlineTerminated())
        assertEquals(
            "--- old\n+++ new\n@@ -1,3 +1,3 @@\n Hello World\n-some stuff here\n+some amazing stuff here\n some more stuff here\n",
            diff
                .unifiedDiff()
                .contextRadius(3)
                .header("old", "new")
                .toString(),
        )
    }

    @Test
    fun testLineOps() {
        val diff =
            TextDiff.fromLines(
                "Hello World\nsome stuff here\nsome more stuff here\n",
                "Hello World\nsome amazing stuff here\nsome more stuff here\n",
            )
        assertTrue(diff.newlineTerminated())
        val changes =
            diff.ops().flatMap { op ->
                diff.iterChanges(op).asSequence().toList()
            }
        assertEquals(
            listOf(
                Change(ChangeTag.Equal, 0, 0, "Hello World\n"),
                Change(ChangeTag.Delete, 1, null, "some stuff here\n"),
                Change(ChangeTag.Insert, null, 1, "some amazing stuff here\n"),
                Change(ChangeTag.Equal, 2, 2, "some more stuff here\n"),
            ),
            changes,
        )
    }

    @Test
    fun testVirtualNewlines() {
        val diff = TextDiff.fromLines("a\nb", "a\nc\n")
        assertTrue(diff.newlineTerminated())
        val changes =
            diff.ops().flatMap { op ->
                diff.iterChanges(op).asSequence().toList()
            }
        assertEquals(
            listOf(
                Change(ChangeTag.Equal, 0, 0, "a\n"),
                Change(ChangeTag.Delete, 1, null, "b"),
                Change(ChangeTag.Insert, null, 1, "c\n"),
            ),
            changes,
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

    @Test
    fun testLifetimesOnIter() {
        val a = "1\n2\n3\n"
        val b = "1\n99\n3\n"
        val diff = TextDiff.fromLines(a, b)
        val changes = diff.iterAllChanges().asSequence().toList()
        assertEquals(
            listOf(
                Change(ChangeTag.Equal, 0, 0, "1\n"),
                Change(ChangeTag.Delete, 1, null, "2\n"),
                Change(ChangeTag.Insert, null, 1, "99\n"),
                Change(ChangeTag.Equal, 2, 2, "3\n"),
            ),
            changes,
        )
    }

    // test_serde and test_serde_ops:
    // Rust crate serde feature serialization tests rely on serde JSON snapshots.
}
