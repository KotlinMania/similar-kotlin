// port-lint: source common.rs
package io.github.kotlinmania.similar

import kotlin.test.Test
import kotlin.test.assertEquals

class CommonTest {
    @Test
    fun testNonStringIterChange() {
        val old = listOf(1, 2, 3)
        val new = listOf(1, 2, 4)
        val changes = captureDiffSlices(Algorithm.Myers, old, new)
            .flatMap { op ->
                val iter = op.iterChanges(old.asLookup(), new.asLookup())
                buildList {
                    while (iter.hasNext()) {
                        val change = iter.next()
                        add(change.tag() to change.value())
                    }
                }
            }

        assertEquals(
            listOf(
                ChangeTag.Equal to 1,
                ChangeTag.Equal to 2,
                ChangeTag.Delete to 3,
                ChangeTag.Insert to 4,
            ),
            changes,
        )
    }
}
