// port-lint: source text/inline.rs
package io.github.kotlinmania.similar.text

import io.github.kotlinmania.similar.ChangeTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InlineTest {
    @Test
    fun testLineOpsInline() {
        val diff =
            TextDiff.fromLines(
                "Hello World\nsome stuff here\nsome more stuff here\n\nAha stuff here\nand more stuff",
                "Stuff\nHello World\nsome amazing stuff here\nsome more stuff here\n",
            )
        assertTrue(diff.newlineTerminated())

        val changes =
            diff.ops().flatMap { op ->
                val iter = diff.iterInlineChanges(op)
                buildList {
                    while (iter.hasNext()) {
                        add(iter.next())
                    }
                }
            }

        assertEquals(
            listOf(
                InlineChange(ChangeTag.Insert, null, 0, listOf(InlineSegment(false, "Stuff\n"))),
                InlineChange(ChangeTag.Equal, 0, 1, listOf(InlineSegment(false, "Hello World\n"))),
                InlineChange(
                    ChangeTag.Delete,
                    1,
                    null,
                    listOf(
                        InlineSegment(false, "some "),
                        InlineSegment(false, "stuff here\n"),
                    ),
                ),
                InlineChange(
                    ChangeTag.Insert,
                    null,
                    2,
                    listOf(
                        InlineSegment(false, "some "),
                        InlineSegment(true, "amazing "),
                        InlineSegment(false, "stuff here\n"),
                    ),
                ),
                InlineChange(ChangeTag.Equal, 2, 3, listOf(InlineSegment(false, "some more stuff here\n"))),
                InlineChange(ChangeTag.Delete, 3, null, listOf(InlineSegment(false, "\n"))),
                InlineChange(ChangeTag.Delete, 4, null, listOf(InlineSegment(false, "Aha stuff here\n"))),
                InlineChange(ChangeTag.Delete, 5, null, listOf(InlineSegment(false, "and more stuff"))),
            ),
            changes,
        )
    }
}
