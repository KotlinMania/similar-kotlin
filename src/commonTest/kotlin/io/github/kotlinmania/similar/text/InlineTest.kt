// port-lint: source text/inline.rs
package io.github.kotlinmania.similar.text

import io.github.kotlinmania.similar.ChangeTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InlineTest {
    @Test
    fun testLineOpsInline() {
        val diff = TextDiff.fromLines(
            "Hello World\nsome stuff here\nsome more stuff here\n\nAha stuff here\nand more stuff",
            "Stuff\nHello World\nsome amazing stuff here\nsome more stuff here\n",
        )
        assertTrue(diff.newlineTerminated())

        val changes = diff.ops().flatMap { op ->
            val iter = diff.iterInlineChanges(op)
            buildList {
                while (iter.hasNext()) {
                    add(iter.next())
                }
            }
        }

        assertEquals(ChangeTag.Insert, changes.first().tag())
        assertEquals("Stuff\n", changes.first().values().single().second)
        assertEquals(ChangeTag.Delete, changes.last().tag())
        assertTrue(changes.any { change ->
            change.tag() == ChangeTag.Insert && change.values().any { (emphasized, value) ->
                emphasized && value.contains("amazing")
            }
        })
    }
}
