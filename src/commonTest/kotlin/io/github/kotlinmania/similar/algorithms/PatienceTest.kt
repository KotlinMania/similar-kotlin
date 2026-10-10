// port-lint: source algorithms/patience.rs
package io.github.kotlinmania.similar.algorithms

import io.github.kotlinmania.similar.DiffOp
import io.github.kotlinmania.similar.asLookup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PatienceTest {
    @Test
    fun testPatience() {
        val old = listOf(11, 1, 2, 2, 3, 4, 4, 4, 5, 47, 19)
        val new = listOf(10, 1, 2, 2, 8, 9, 4, 4, 7, 47, 18)
        val d = Replace(Capture())

        patienceDiff(d, old.asLookup(), old.indices, new.asLookup(), new.indices)

        val ops = d.intoInner().ops()
        assertEquals(
            listOf(
                DiffOp.Replace(0, 1, 0, 1),
                DiffOp.Equal(1, 1, 3),
                DiffOp.Replace(4, 1, 4, 2),
                DiffOp.Equal(5, 6, 2),
                DiffOp.Replace(7, 2, 8, 1),
                DiffOp.Equal(9, 9, 1),
                DiffOp.Replace(10, 1, 10, 1),
            ),
            ops,
        )
        assertEquals(new, applyOps(old, new, ops))
    }

    @Test
    fun testPatienceOutOfBoundsBug() {
        val old = listOf(1, 2, 3, 4)
        val new = listOf(1, 2, 3)
        val d = Replace(Capture())

        patienceDiff(d, old.asLookup(), old.indices, new.asLookup(), new.indices)

        assertEquals(
            listOf(
                DiffOp.Equal(0, 0, 3),
                DiffOp.Delete(3, 1, 3),
            ),
            d.intoInner().ops(),
        )
    }

    @Test
    fun testFinishCalled() {
        val slice = listOf(1, 2)
        val slice2 = listOf(1, 2, 3)
        val d = PatienceFinishHook()

        patienceDiff(d, slice.asLookup(), slice.indices, slice2.asLookup(), slice2.indices)
        assertTrue(d.finished)

        val same = PatienceFinishHook()
        patienceDiff(same, slice.asLookup(), slice.indices, slice.asLookup(), slice.indices)
        assertTrue(same.finished)

        val empty = PatienceFinishHook()
        patienceDiff(empty, emptyList<Int>().asLookup(), IntRange.EMPTY, emptyList<Int>().asLookup(), IntRange.EMPTY)
        assertTrue(empty.finished)
    }
}

private class PatienceFinishHook : DiffHook<Nothing> {
    var finished: Boolean = false

    override fun finish(): DiffHookResult<Nothing> {
        finished = true
        return DiffHookResult.Ok
    }
}

private fun <T> applyOps(old: List<T>, new: List<T>, ops: List<DiffOp>): List<T> =
    buildList {
        for (op in ops) {
            when (op) {
                is DiffOp.Equal -> {
                    addAll(old.subList(op.oldIndex, op.oldIndex + op.len))
                }

                is DiffOp.Delete -> {}

                is DiffOp.Insert -> {
                    addAll(new.subList(op.newIndex, op.newIndex + op.newLen))
                }

                is DiffOp.Replace -> {
                    addAll(new.subList(op.newIndex, op.newIndex + op.newLen))
                }
            }
        }
    }
